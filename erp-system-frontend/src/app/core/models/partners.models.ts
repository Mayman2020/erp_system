export interface PartnerDto {
  id: number;
  code: string;
  name: string;
  sharePercent: number;
  capitalAccountId?: number | null;
  capitalAccountCode?: string;
  capitalAccountName?: string;
  drawingAccountId?: number | null;
  drawingAccountCode?: string;
  drawingAccountName?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface PartnerForm {
  code: string;
  name: string;
  sharePercent: number;
  capitalAccountId?: number | null;
  drawingAccountId?: number | null;
  active?: boolean;
}

export interface PartnerTransactionDto {
  id: number;
  partnerId: number;
  partnerCode?: string;
  partnerName?: string;
  txnType: string;
  amount: number;
  txnDate: string;
  notes?: string;
  status: string;
  journalEntryId?: number;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface PartnerTransactionForm {
  partnerId: number;
  txnType: string;
  amount: number;
  txnDate: string;
  notes?: string;
}

export interface ProfitDistributionLineDto {
  id?: number;
  partnerId: number;
  partnerCode?: string;
  partnerName?: string;
  sharePercent: number;
  amount: number;
}

export interface ProfitDistributionDto {
  id: number;
  distributionNo: string;
  periodLabel: string;
  totalProfit: number;
  status: string;
  approvedAt?: string;
  journalEntryId?: number;
  lines: ProfitDistributionLineDto[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface ProfitDistributionForm {
  distributionNo?: string;
  periodLabel: string;
  totalProfit?: number;
  profitFromDate?: string;
  profitToDate?: string;
}
