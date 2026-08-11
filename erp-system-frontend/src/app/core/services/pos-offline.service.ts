import { Injectable } from '@angular/core';
import { from, fromEvent, merge, Observable, of, firstValueFrom } from 'rxjs';
import { map } from 'rxjs/operators';
import { PosOfflineSyncForm, PosSaleForm } from '../models/erp.models';
import { PosApiService } from './pos-api.service';

interface QueuedPosSale {
  id: string;
  batchKey: string;
  terminalId?: number;
  payload: PosSaleForm;
  createdAt: number;
}

const DB_NAME = 'erp-pos-offline';
const STORE = 'sales-queue';

@Injectable({ providedIn: 'root' })
export class PosOfflineService {
  online$ = merge(
    of(typeof navigator !== 'undefined' ? navigator.onLine : true),
    fromEvent(window, 'online').pipe(map(() => true)),
    fromEvent(window, 'offline').pipe(map(() => false))
  );

  constructor(private posApi: PosApiService) {
    this.online$.subscribe((online) => {
      if (online) {
        void this.flush();
      }
    });
  }

  isOnline(): boolean {
    return typeof navigator === 'undefined' ? true : navigator.onLine;
  }

  pendingCount(): Observable<number> {
    return from(this.readAll()).pipe(map((records) => records.length));
  }

  async enqueueSale(sale: PosSaleForm, terminalId?: number): Promise<void> {
    const batchKey = sale.offlineBatchId || `batch-${new Date().toISOString().slice(0, 10)}-${terminalId || 'local'}`;
    const record: QueuedPosSale = {
      id: crypto.randomUUID(),
      batchKey,
      terminalId,
      payload: { ...sale, offlineBatchId: batchKey },
      createdAt: Date.now()
    };
    await this.putRecord(record);
  }

  queueSale(batchKey: string, terminalId: number | undefined, payload: PosSaleForm): Observable<void> {
    return from(this.enqueueSale({ ...payload, offlineBatchId: batchKey }, terminalId));
  }

  syncPending(): Observable<number> {
    return from(this.flush()).pipe(map((count) => count));
  }

  async flush(): Promise<number> {
    if (!this.isOnline()) {
      return 0;
    }
    const records = await this.readAll();
    if (!records.length) {
      return 0;
    }
    const grouped = records.reduce<Record<string, QueuedPosSale[]>>((acc, record) => {
      acc[record.batchKey] = acc[record.batchKey] || [];
      acc[record.batchKey].push(record);
      return acc;
    }, {});

    let processed = 0;
    for (const [batchKey, items] of Object.entries(grouped)) {
      const payload: PosOfflineSyncForm = {
        batchKey,
        terminalId: items[0]?.terminalId,
        sales: items.map((item) => item.payload)
      };
      try {
        await firstValueFrom(this.posApi.syncOfflineBatch(payload));
        await this.deleteRecords(items.map((item) => item.id));
        processed += items.length;
      } catch {
        // keep queue for retry
      }
    }
    return processed;
  }

  private openDb(): Promise<IDBDatabase> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, 1);
      request.onupgradeneeded = () => {
        const db = request.result;
        if (!db.objectStoreNames.contains(STORE)) {
          db.createObjectStore(STORE, { keyPath: 'id' });
        }
      };
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  private putRecord(record: QueuedPosSale): Promise<void> {
    return this.openDb().then(
      (db) =>
        new Promise((resolve, reject) => {
          const tx = db.transaction(STORE, 'readwrite');
          tx.objectStore(STORE).add(record);
          tx.oncomplete = () => resolve();
          tx.onerror = () => reject(tx.error);
        })
    );
  }

  private readAll(): Promise<QueuedPosSale[]> {
    return this.openDb().then(
      (db) =>
        new Promise((resolve, reject) => {
          const tx = db.transaction(STORE, 'readonly');
          const request = tx.objectStore(STORE).getAll();
          request.onsuccess = () => resolve((request.result as QueuedPosSale[]) || []);
          request.onerror = () => reject(request.error);
        })
    );
  }

  private deleteRecords(ids: string[]): Promise<void> {
    if (!ids.length) {
      return Promise.resolve();
    }
    return this.openDb().then(
      (db) =>
        new Promise((resolve, reject) => {
          const tx = db.transaction(STORE, 'readwrite');
          const store = tx.objectStore(STORE);
          ids.forEach((id) => store.delete(id));
          tx.oncomplete = () => resolve();
          tx.onerror = () => reject(tx.error);
        })
    );
  }
}
