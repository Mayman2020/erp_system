/** Default rows per table page (estate-style lists). */
export const DEFAULT_TABLE_PAGE_SIZE = 5;

export function paginatedSlice<T>(
  items: readonly T[] | T[] | null | undefined,
  pageIndex: number,
  pageSize: number = DEFAULT_TABLE_PAGE_SIZE
): T[] {
  const list = items ?? [];
  const start = Math.max(0, pageIndex) * pageSize;
  return list.slice(start, start + pageSize);
}

export function clampTablePageIndex(
  pageIndex: number,
  length: number,
  pageSize: number = DEFAULT_TABLE_PAGE_SIZE
): number {
  const max = Math.max(0, Math.ceil(length / pageSize) - 1);
  return Math.min(Math.max(0, pageIndex), max);
}

export function withPageParams(
  page: number,
  size: number,
  extra?: Record<string, string | number | boolean | null | undefined>
): Record<string, string | number | boolean> {
  const params: Record<string, string | number | boolean> = {
    page,
    size
  };
  if (extra) {
    for (const [key, value] of Object.entries(extra)) {
      if (value !== undefined && value !== null && value !== '') {
        params[key] = value as string | number | boolean;
      }
    }
  }
  return params;
}
