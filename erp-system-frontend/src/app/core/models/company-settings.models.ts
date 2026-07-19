export interface CompanySettings {
  companyNameEn: string;
  companyNameAr: string;
  taxId?: string | null;
  logoBase64?: string | null;
  fiscalYearStartMonth: number;
}

export type CompanySettingsForm = CompanySettings;
