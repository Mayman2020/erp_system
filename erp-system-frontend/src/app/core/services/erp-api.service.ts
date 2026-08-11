import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.models';
import {
  AttendanceRecordDto,
  CrmActivityDto,
  CrmLeadDto,
  CustomerDto,
  DepartmentDto,
  EmployeeDto,
  ErpInventoryReportDto,
  ErpProfitReportDto,
  ErpDashboardDto,
  ActivityLogDto,
  ErpPurchasesReportDto,
  ErpSalesReportDto,
  LeaveRequestDto,
  PayrollRunDto,
  ProductCategoryDto,
  ProductDto,
  ProjectDto,
  PurchaseInvoiceDto,
  PurchaseInvoiceForm,
  PurchaseOrderDto,
  PurchaseOrderForm,
  PurchaseReturnDto,
  PurchaseReturnForm,
  SalesInvoiceDto,
  SalesInvoiceForm,
  SalesOrderDto,
  SalesOrderForm,
  SalesQuotationDto,
  SalesQuotationForm,
  SalesReturnDto,
  SalesReturnForm,
  StockLevelDto,
  StockMovementDto,
  SupplierDto,
  SupplierPaymentDto,
  WarehouseDto,
  WorkOrderDto,
  WorkOrderForm,
  MaintenanceAssetDto,
  MaintenanceAssetForm,
  MaintenanceTechnicianDto,
  MaintenanceTechnicianForm,
  MaintenanceTicketDto,
  MaintenanceTicketForm,
  MaintenanceChecklistForm,
  MaintenanceSparePartForm,
  MaintenanceChecklistDto,
  MaintenanceSparePartDto,
  AssignTechnicianForm,
  UnitOfMeasureDto,
  UnitOfMeasureForm,
  ProductForm,
  ProductCategoryForm,
  WarehouseForm,
  StockMovementForm,
  CustomerForm,
  SupplierForm,
  SupplierPaymentForm,
  DepartmentForm,
  EmployeeForm,
  AttendanceRecordForm,
  LeaveRequestForm,
  PayrollRunForm,
  PayrollLineDto,
  PayrollLineForm,
  EmployeeDocumentDto,
  EmployeeDocumentForm,
  ProductBomLineDto,
  ProductBomLineForm,
  LowStockAlertDto,
  CrmLeadForm,
  CrmActivityForm,
  CrmNoteDto,
  CrmNoteForm,
  ProjectForm,
  ProjectTaskDto,
  ProjectTaskForm,
  ProjectMemberDto,
  ProjectMemberForm,
  ProjectExpenseDto,
  ProjectExpenseForm,
  LabelPreviewDto,
  StockIncidentDto,
  StockIncidentForm,
  ReplenishmentProposalDto,
  PurchaseRfqDto,
  PurchaseRfqForm,
  GoodsReceiptDto,
  GoodsReceiptForm,
  HrVacancyDto,
  HrVacancyForm,
  HrCandidateDto,
  HrCandidateForm,
  HrInterviewDto,
  HrInterviewForm,
  LeaveBalanceDto,
  PmoMilestoneDto,
  PmoMilestoneForm,
  PmoRiskDto,
  PmoRiskForm,
  PmoIssueDto,
  PmoIssueForm,
  DigitalCourseDto,
  DigitalCourseForm,
  DigitalEnrollmentDto,
  DigitalEnrollmentForm,
  LicenseDto,
  LicenseActivateForm,
  BackupJobDto,
  AlertEventDto
} from '../models/erp.models';

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  first: boolean;
  last: boolean;
}

export type PagedResult<T> = PagedResponse<T>;
export type PagedQuery = Record<string, string | number | boolean>;

@Injectable({ providedIn: 'root' })
export class ErpApiService {
  private readonly inventoryBase = `${environment.apiUrl}/inventory`;
  private readonly salesBase = `${environment.apiUrl}/sales`;
  private readonly purchasesBase = `${environment.apiUrl}/purchases`;
  private readonly hrBase = `${environment.apiUrl}/hr`;
  private readonly crmBase = `${environment.apiUrl}/crm`;
  private readonly projectsBase = `${environment.apiUrl}/projects`;
  private readonly manufacturingBase = `${environment.apiUrl}/manufacturing`;
  private readonly maintenanceBase = `${environment.apiUrl}/maintenance`;
  private readonly erpBase = `${environment.apiUrl}/erp`;
  private readonly pmoBase = `${environment.apiUrl}/pmo`;
  private readonly digitalLiteracyBase = `${environment.apiUrl}/digital-literacy`;
  private readonly alertsBase = `${environment.apiUrl}/alerts`;
  private readonly adminBase = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  // Inventory
  getProducts(filters: Record<string, string | number | boolean> = {}): Observable<ProductDto[]> {
    return this.http
      .get<ApiResponse<ProductDto[]>>(`${this.inventoryBase}/products`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getProductsPaged(filters: PagedQuery = {}): Observable<PagedResult<ProductDto>> {
    return this.getPaged(`${this.inventoryBase}/products/paged`, filters);
  }

  getCategories(filters: Record<string, string | number | boolean> = {}): Observable<ProductCategoryDto[]> {
    return this.http
      .get<ApiResponse<ProductCategoryDto[]>>(`${this.inventoryBase}/categories`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getCategoriesPaged(filters: PagedQuery = {}): Observable<PagedResult<ProductCategoryDto>> {
    return this.getPaged(`${this.inventoryBase}/categories/paged`, filters);
  }

  getWarehouses(filters: Record<string, string | number | boolean> = {}): Observable<WarehouseDto[]> {
    return this.http
      .get<ApiResponse<WarehouseDto[]>>(`${this.inventoryBase}/warehouses`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getWarehousesPaged(filters: PagedQuery = {}): Observable<PagedResult<WarehouseDto>> {
    return this.getPaged(`${this.inventoryBase}/warehouses/paged`, filters);
  }

  getStockLevels(filters: Record<string, string | number | boolean> = {}): Observable<StockLevelDto[]> {
    return this.http
      .get<ApiResponse<StockLevelDto[]>>(`${this.inventoryBase}/stock/levels`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getStockMovements(filters: Record<string, string | number | boolean> = {}): Observable<StockMovementDto[]> {
    return this.http
      .get<ApiResponse<StockMovementDto[]>>(`${this.inventoryBase}/stock/movements`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getStockMovementsPaged(filters: PagedQuery = {}): Observable<PagedResult<StockMovementDto>> {
    return this.getPaged(`${this.inventoryBase}/stock/movements/paged`, filters);
  }

  // Sales
  getCustomers(filters: Record<string, string | number | boolean> = {}): Observable<CustomerDto[]> {
    return this.http
      .get<ApiResponse<CustomerDto[]>>(`${this.salesBase}/customers`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getCustomersPaged(filters: PagedQuery = {}): Observable<PagedResult<CustomerDto>> {
    return this.getPaged(`${this.salesBase}/customers/paged`, filters);
  }

  getSalesQuotations(filters: Record<string, string | number | boolean> = {}): Observable<SalesQuotationDto[]> {
    return this.http
      .get<ApiResponse<SalesQuotationDto[]>>(`${this.salesBase}/quotations`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getSalesQuotation(id: number): Observable<SalesQuotationDto> {
    return this.http.get<ApiResponse<SalesQuotationDto>>(`${this.salesBase}/quotations/${id}`).pipe(map((res) => res.data));
  }

  createSalesQuotation(payload: SalesQuotationForm): Observable<SalesQuotationDto> {
    return this.http.post<ApiResponse<SalesQuotationDto>>(`${this.salesBase}/quotations`, payload).pipe(map((res) => res.data));
  }

  updateSalesQuotation(id: number, payload: SalesQuotationForm): Observable<SalesQuotationDto> {
    return this.http.put<ApiResponse<SalesQuotationDto>>(`${this.salesBase}/quotations/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteSalesQuotation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.salesBase}/quotations/${id}`);
  }

  approveSalesQuotation(id: number, actor: string): Observable<SalesQuotationDto> {
    return this.http.post<ApiResponse<SalesQuotationDto>>(`${this.salesBase}/quotations/${id}/approve`, null, { params: { actor } }).pipe(map((res) => res.data));
  }

  cancelSalesQuotation(id: number, actor: string, reason?: string): Observable<SalesQuotationDto> {
    return this.http.post<ApiResponse<SalesQuotationDto>>(`${this.salesBase}/quotations/${id}/cancel`, null, { params: this.toParams({ actor, reason }) }).pipe(map((res) => res.data));
  }

  getSalesOrders(filters: Record<string, string | number | boolean> = {}): Observable<SalesOrderDto[]> {
    return this.http
      .get<ApiResponse<SalesOrderDto[]>>(`${this.salesBase}/orders`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getSalesOrder(id: number): Observable<SalesOrderDto> {
    return this.http.get<ApiResponse<SalesOrderDto>>(`${this.salesBase}/orders/${id}`).pipe(map((res) => res.data));
  }

  createSalesOrder(payload: SalesOrderForm): Observable<SalesOrderDto> {
    return this.http.post<ApiResponse<SalesOrderDto>>(`${this.salesBase}/orders`, payload).pipe(map((res) => res.data));
  }

  updateSalesOrder(id: number, payload: SalesOrderForm): Observable<SalesOrderDto> {
    return this.http.put<ApiResponse<SalesOrderDto>>(`${this.salesBase}/orders/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteSalesOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.salesBase}/orders/${id}`);
  }

  approveSalesOrder(id: number, actor: string): Observable<SalesOrderDto> {
    return this.http.post<ApiResponse<SalesOrderDto>>(`${this.salesBase}/orders/${id}/approve`, null, { params: { actor } }).pipe(map((res) => res.data));
  }

  cancelSalesOrder(id: number, actor: string, reason?: string): Observable<SalesOrderDto> {
    return this.http.post<ApiResponse<SalesOrderDto>>(`${this.salesBase}/orders/${id}/cancel`, null, { params: this.toParams({ actor, reason }) }).pipe(map((res) => res.data));
  }

  getSalesInvoices(filters: Record<string, string | number | boolean> = {}): Observable<SalesInvoiceDto[]> {
    return this.http
      .get<ApiResponse<SalesInvoiceDto[]>>(`${this.salesBase}/invoices`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getSalesInvoicesPaged(filters: PagedQuery = {}): Observable<PagedResult<SalesInvoiceDto>> {
    return this.getPaged(`${this.salesBase}/invoices/paged`, filters);
  }

  getSalesInvoice(id: number): Observable<SalesInvoiceDto> {
    return this.http
      .get<ApiResponse<SalesInvoiceDto>>(`${this.salesBase}/invoices/${id}`)
      .pipe(map((res) => res.data));
  }

  createSalesInvoice(payload: SalesInvoiceForm): Observable<SalesInvoiceDto> {
    return this.http
      .post<ApiResponse<SalesInvoiceDto>>(`${this.salesBase}/invoices`, payload)
      .pipe(map((res) => res.data));
  }

  updateSalesInvoice(id: number, payload: SalesInvoiceForm): Observable<SalesInvoiceDto> {
    return this.http
      .put<ApiResponse<SalesInvoiceDto>>(`${this.salesBase}/invoices/${id}`, payload)
      .pipe(map((res) => res.data));
  }

  deleteSalesInvoice(id: number): Observable<void> {
    return this.http.delete<void>(`${this.salesBase}/invoices/${id}`);
  }

  approveSalesInvoice(id: number, actor: string): Observable<SalesInvoiceDto> {
    return this.http
      .post<ApiResponse<SalesInvoiceDto>>(`${this.salesBase}/invoices/${id}/approve`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  cancelSalesInvoice(id: number, actor: string, reason?: string): Observable<SalesInvoiceDto> {
    return this.http
      .post<ApiResponse<SalesInvoiceDto>>(`${this.salesBase}/invoices/${id}/cancel`, null, { params: this.toParams({ actor, reason }) })
      .pipe(map((res) => res.data));
  }

  getSalesReturns(filters: Record<string, string | number | boolean> = {}): Observable<SalesReturnDto[]> {
    return this.http
      .get<ApiResponse<SalesReturnDto[]>>(`${this.salesBase}/returns`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getSalesReturn(id: number): Observable<SalesReturnDto> {
    return this.http.get<ApiResponse<SalesReturnDto>>(`${this.salesBase}/returns/${id}`).pipe(map((res) => res.data));
  }

  createSalesReturn(payload: SalesReturnForm): Observable<SalesReturnDto> {
    return this.http.post<ApiResponse<SalesReturnDto>>(`${this.salesBase}/returns`, payload).pipe(map((res) => res.data));
  }

  updateSalesReturn(id: number, payload: SalesReturnForm): Observable<SalesReturnDto> {
    return this.http.put<ApiResponse<SalesReturnDto>>(`${this.salesBase}/returns/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteSalesReturn(id: number): Observable<void> {
    return this.http.delete<void>(`${this.salesBase}/returns/${id}`);
  }

  approveSalesReturn(id: number, actor: string): Observable<SalesReturnDto> {
    return this.http.post<ApiResponse<SalesReturnDto>>(`${this.salesBase}/returns/${id}/approve`, null, { params: { actor } }).pipe(map((res) => res.data));
  }

  cancelSalesReturn(id: number, actor: string, reason?: string): Observable<SalesReturnDto> {
    return this.http.post<ApiResponse<SalesReturnDto>>(`${this.salesBase}/returns/${id}/cancel`, null, { params: this.toParams({ actor, reason }) }).pipe(map((res) => res.data));
  }

  // Purchases
  getSuppliers(filters: Record<string, string | number | boolean> = {}): Observable<SupplierDto[]> {
    return this.http
      .get<ApiResponse<SupplierDto[]>>(`${this.purchasesBase}/suppliers`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getSuppliersPaged(filters: PagedQuery = {}): Observable<PagedResult<SupplierDto>> {
    return this.getPaged(`${this.purchasesBase}/suppliers/paged`, filters);
  }

  getPurchaseOrders(filters: Record<string, string | number | boolean> = {}): Observable<PurchaseOrderDto[]> {
    return this.http
      .get<ApiResponse<PurchaseOrderDto[]>>(`${this.purchasesBase}/orders`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getPurchaseOrdersPaged(filters: PagedQuery = {}): Observable<PagedResult<PurchaseOrderDto>> {
    return this.getPaged(`${this.purchasesBase}/orders/paged`, filters);
  }

  getPurchaseOrder(id: number): Observable<PurchaseOrderDto> {
    return this.http.get<ApiResponse<PurchaseOrderDto>>(`${this.purchasesBase}/orders/${id}`).pipe(map((res) => res.data));
  }

  createPurchaseOrder(payload: PurchaseOrderForm): Observable<PurchaseOrderDto> {
    return this.http.post<ApiResponse<PurchaseOrderDto>>(`${this.purchasesBase}/orders`, payload).pipe(map((res) => res.data));
  }

  updatePurchaseOrder(id: number, payload: PurchaseOrderForm): Observable<PurchaseOrderDto> {
    return this.http.put<ApiResponse<PurchaseOrderDto>>(`${this.purchasesBase}/orders/${id}`, payload).pipe(map((res) => res.data));
  }

  deletePurchaseOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.purchasesBase}/orders/${id}`);
  }

  approvePurchaseOrder(id: number, actor: string): Observable<PurchaseOrderDto> {
    return this.http.post<ApiResponse<PurchaseOrderDto>>(`${this.purchasesBase}/orders/${id}/approve`, null, { params: { actor } }).pipe(map((res) => res.data));
  }

  cancelPurchaseOrder(id: number, actor: string, reason?: string): Observable<PurchaseOrderDto> {
    return this.http.post<ApiResponse<PurchaseOrderDto>>(`${this.purchasesBase}/orders/${id}/cancel`, null, { params: this.toParams({ actor, reason }) }).pipe(map((res) => res.data));
  }

  getPurchaseInvoices(filters: Record<string, string | number | boolean> = {}): Observable<PurchaseInvoiceDto[]> {
    return this.http
      .get<ApiResponse<PurchaseInvoiceDto[]>>(`${this.purchasesBase}/invoices`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getPurchaseInvoice(id: number): Observable<PurchaseInvoiceDto> {
    return this.http
      .get<ApiResponse<PurchaseInvoiceDto>>(`${this.purchasesBase}/invoices/${id}`)
      .pipe(map((res) => res.data));
  }

  createPurchaseInvoice(payload: PurchaseInvoiceForm): Observable<PurchaseInvoiceDto> {
    return this.http
      .post<ApiResponse<PurchaseInvoiceDto>>(`${this.purchasesBase}/invoices`, payload)
      .pipe(map((res) => res.data));
  }

  updatePurchaseInvoice(id: number, payload: PurchaseInvoiceForm): Observable<PurchaseInvoiceDto> {
    return this.http
      .put<ApiResponse<PurchaseInvoiceDto>>(`${this.purchasesBase}/invoices/${id}`, payload)
      .pipe(map((res) => res.data));
  }

  deletePurchaseInvoice(id: number): Observable<void> {
    return this.http.delete<void>(`${this.purchasesBase}/invoices/${id}`);
  }

  approvePurchaseInvoice(id: number, actor: string): Observable<PurchaseInvoiceDto> {
    return this.http
      .post<ApiResponse<PurchaseInvoiceDto>>(`${this.purchasesBase}/invoices/${id}/approve`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  cancelPurchaseInvoice(id: number, actor: string, reason?: string): Observable<PurchaseInvoiceDto> {
    return this.http
      .post<ApiResponse<PurchaseInvoiceDto>>(`${this.purchasesBase}/invoices/${id}/cancel`, null, { params: this.toParams({ actor, reason }) })
      .pipe(map((res) => res.data));
  }

  getPurchaseReturns(filters: Record<string, string | number | boolean> = {}): Observable<PurchaseReturnDto[]> {
    return this.http
      .get<ApiResponse<PurchaseReturnDto[]>>(`${this.purchasesBase}/returns`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getPurchaseReturn(id: number): Observable<PurchaseReturnDto> {
    return this.http.get<ApiResponse<PurchaseReturnDto>>(`${this.purchasesBase}/returns/${id}`).pipe(map((res) => res.data));
  }

  createPurchaseReturn(payload: PurchaseReturnForm): Observable<PurchaseReturnDto> {
    return this.http.post<ApiResponse<PurchaseReturnDto>>(`${this.purchasesBase}/returns`, payload).pipe(map((res) => res.data));
  }

  updatePurchaseReturn(id: number, payload: PurchaseReturnForm): Observable<PurchaseReturnDto> {
    return this.http.put<ApiResponse<PurchaseReturnDto>>(`${this.purchasesBase}/returns/${id}`, payload).pipe(map((res) => res.data));
  }

  deletePurchaseReturn(id: number): Observable<void> {
    return this.http.delete<void>(`${this.purchasesBase}/returns/${id}`);
  }

  approvePurchaseReturn(id: number, actor: string): Observable<PurchaseReturnDto> {
    return this.http.post<ApiResponse<PurchaseReturnDto>>(`${this.purchasesBase}/returns/${id}/approve`, null, { params: { actor } }).pipe(map((res) => res.data));
  }

  cancelPurchaseReturn(id: number, actor: string, reason?: string): Observable<PurchaseReturnDto> {
    return this.http.post<ApiResponse<PurchaseReturnDto>>(`${this.purchasesBase}/returns/${id}/cancel`, null, { params: this.toParams({ actor, reason }) }).pipe(map((res) => res.data));
  }

  getSupplierPayments(filters: Record<string, string | number | boolean> = {}): Observable<SupplierPaymentDto[]> {
    return this.http
      .get<ApiResponse<SupplierPaymentDto[]>>(`${this.purchasesBase}/payments`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  // HR
  getDepartments(filters: Record<string, string | number | boolean> = {}): Observable<DepartmentDto[]> {
    return this.http
      .get<ApiResponse<DepartmentDto[]>>(`${this.hrBase}/departments`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getEmployees(filters: Record<string, string | number | boolean> = {}): Observable<EmployeeDto[]> {
    return this.http
      .get<ApiResponse<EmployeeDto[]>>(`${this.hrBase}/employees`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getEmployeesPaged(filters: PagedQuery = {}): Observable<PagedResult<EmployeeDto>> {
    return this.getPaged(`${this.hrBase}/employees/paged`, filters);
  }

  getAttendanceRecords(filters: Record<string, string | number | boolean> = {}): Observable<AttendanceRecordDto[]> {
    return this.http
      .get<ApiResponse<AttendanceRecordDto[]>>(`${this.hrBase}/attendance`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getAttendanceRecordsPaged(filters: PagedQuery = {}): Observable<PagedResult<AttendanceRecordDto>> {
    return this.getPaged(`${this.hrBase}/attendance/paged`, filters);
  }

  getLeaveRequests(filters: Record<string, string | number | boolean> = {}): Observable<LeaveRequestDto[]> {
    return this.http
      .get<ApiResponse<LeaveRequestDto[]>>(`${this.hrBase}/leave-requests`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getPayrollRuns(filters: Record<string, string | number | boolean> = {}): Observable<PayrollRunDto[]> {
    return this.http
      .get<ApiResponse<PayrollRunDto[]>>(`${this.hrBase}/payroll`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  // CRM
  getLeads(filters: Record<string, string | number | boolean> = {}): Observable<CrmLeadDto[]> {
    return this.http
      .get<ApiResponse<CrmLeadDto[]>>(`${this.crmBase}/leads`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getCrmActivities(filters: Record<string, string | number | boolean> = {}): Observable<CrmActivityDto[]> {
    return this.http
      .get<ApiResponse<CrmActivityDto[]>>(`${this.crmBase}/activities`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  // Projects
  getProjects(filters: Record<string, string | number | boolean> = {}): Observable<ProjectDto[]> {
    return this.http
      .get<ApiResponse<ProjectDto[]>>(`${this.projectsBase}`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  // ERP Reports
  getSalesReport(fromDate?: string, toDate?: string): Observable<ErpSalesReportDto> {
    return this.http
      .get<ApiResponse<ErpSalesReportDto>>(`${this.erpBase}/reports/sales`, { params: this.toParams({ fromDate, toDate }) })
      .pipe(map((res) => res.data));
  }

  getPurchasesReport(fromDate?: string, toDate?: string): Observable<ErpPurchasesReportDto> {
    return this.http
      .get<ApiResponse<ErpPurchasesReportDto>>(`${this.erpBase}/reports/purchases`, { params: this.toParams({ fromDate, toDate }) })
      .pipe(map((res) => res.data));
  }

  getInventoryReport(): Observable<ErpInventoryReportDto> {
    return this.http
      .get<ApiResponse<ErpInventoryReportDto>>(`${this.erpBase}/reports/inventory`)
      .pipe(map((res) => res.data));
  }

  getProfitReport(fromDate?: string, toDate?: string): Observable<ErpProfitReportDto> {
    return this.http
      .get<ApiResponse<ErpProfitReportDto>>(`${this.erpBase}/reports/profit`, { params: this.toParams({ fromDate, toDate }) })
      .pipe(map((res) => res.data));
  }

  getErpDashboard(filters: { fromDate?: string; toDate?: string } = {}): Observable<ErpDashboardDto> {
    return this.http
      .get<ApiResponse<ErpDashboardDto>>(`${this.erpBase}/dashboard`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data));
  }

  getActivityLogs(page = 0, size = 10): Observable<{ items: ActivityLogDto[] }> {
    return this.http
      .get<ApiResponse<{ items: ActivityLogDto[] }>>(`${this.erpBase}/activity-logs`, { params: this.toParams({ page, size }) })
      .pipe(map((res) => res.data || { items: [] }));
  }

  // Manufacturing
  getWorkOrders(): Observable<WorkOrderDto[]> {
    return this.http
      .get<ApiResponse<WorkOrderDto[]>>(`${this.manufacturingBase}/work-orders`)
      .pipe(map((res) => res.data || []));
  }

  getWorkOrder(id: number): Observable<WorkOrderDto> {
    return this.http
      .get<ApiResponse<WorkOrderDto>>(`${this.manufacturingBase}/work-orders/${id}`)
      .pipe(map((res) => res.data));
  }

  createWorkOrder(payload: WorkOrderForm): Observable<WorkOrderDto> {
    return this.http
      .post<ApiResponse<WorkOrderDto>>(`${this.manufacturingBase}/work-orders`, payload)
      .pipe(map((res) => res.data));
  }

  updateWorkOrder(id: number, payload: WorkOrderForm): Observable<WorkOrderDto> {
    return this.http
      .put<ApiResponse<WorkOrderDto>>(`${this.manufacturingBase}/work-orders/${id}`, payload)
      .pipe(map((res) => res.data));
  }

  deleteWorkOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.manufacturingBase}/work-orders/${id}`);
  }

  startWorkOrder(id: number, actor: string): Observable<WorkOrderDto> {
    return this.http
      .post<ApiResponse<WorkOrderDto>>(`${this.manufacturingBase}/work-orders/${id}/start`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  completeWorkOrder(id: number, actor: string): Observable<WorkOrderDto> {
    return this.http
      .post<ApiResponse<WorkOrderDto>>(`${this.manufacturingBase}/work-orders/${id}/complete`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  cancelWorkOrder(id: number, actor: string): Observable<WorkOrderDto> {
    return this.http
      .post<ApiResponse<WorkOrderDto>>(`${this.manufacturingBase}/work-orders/${id}/cancel`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  // Maintenance
  getMaintenanceAssets(status?: string): Observable<MaintenanceAssetDto[]> {
    return this.http
      .get<ApiResponse<MaintenanceAssetDto[]>>(`${this.maintenanceBase}/assets`, { params: this.toParams({ status }) })
      .pipe(map((res) => res.data || []));
  }

  getMaintenanceAsset(id: number): Observable<MaintenanceAssetDto> {
    return this.http
      .get<ApiResponse<MaintenanceAssetDto>>(`${this.maintenanceBase}/assets/${id}`)
      .pipe(map((res) => res.data));
  }

  createMaintenanceAsset(payload: MaintenanceAssetForm): Observable<MaintenanceAssetDto> {
    return this.http
      .post<ApiResponse<MaintenanceAssetDto>>(`${this.maintenanceBase}/assets`, payload)
      .pipe(map((res) => res.data));
  }

  updateMaintenanceAsset(id: number, payload: MaintenanceAssetForm): Observable<MaintenanceAssetDto> {
    return this.http
      .put<ApiResponse<MaintenanceAssetDto>>(`${this.maintenanceBase}/assets/${id}`, payload)
      .pipe(map((res) => res.data));
  }

  deleteMaintenanceAsset(id: number): Observable<void> {
    return this.http.delete<void>(`${this.maintenanceBase}/assets/${id}`);
  }

  getMaintenanceTechnicians(activeOnly = true): Observable<MaintenanceTechnicianDto[]> {
    return this.http
      .get<ApiResponse<MaintenanceTechnicianDto[]>>(`${this.maintenanceBase}/technicians`, { params: this.toParams({ activeOnly }) })
      .pipe(map((res) => res.data || []));
  }

  getMaintenanceTechnician(id: number): Observable<MaintenanceTechnicianDto> {
    return this.http
      .get<ApiResponse<MaintenanceTechnicianDto>>(`${this.maintenanceBase}/technicians/${id}`)
      .pipe(map((res) => res.data));
  }

  createMaintenanceTechnician(payload: MaintenanceTechnicianForm): Observable<MaintenanceTechnicianDto> {
    return this.http
      .post<ApiResponse<MaintenanceTechnicianDto>>(`${this.maintenanceBase}/technicians`, payload)
      .pipe(map((res) => res.data));
  }

  updateMaintenanceTechnician(id: number, payload: MaintenanceTechnicianForm): Observable<MaintenanceTechnicianDto> {
    return this.http
      .put<ApiResponse<MaintenanceTechnicianDto>>(`${this.maintenanceBase}/technicians/${id}`, payload)
      .pipe(map((res) => res.data));
  }

  deleteMaintenanceTechnician(id: number): Observable<void> {
    return this.http.delete<void>(`${this.maintenanceBase}/technicians/${id}`);
  }

  getMaintenanceTickets(status?: string): Observable<MaintenanceTicketDto[]> {
    return this.http
      .get<ApiResponse<MaintenanceTicketDto[]>>(`${this.maintenanceBase}/tickets`, { params: this.toParams({ status }) })
      .pipe(map((res) => res.data || []));
  }

  getMaintenanceTicket(id: number): Observable<MaintenanceTicketDto> {
    return this.http
      .get<ApiResponse<MaintenanceTicketDto>>(`${this.maintenanceBase}/tickets/${id}`)
      .pipe(map((res) => res.data));
  }

  createMaintenanceTicket(payload: MaintenanceTicketForm): Observable<MaintenanceTicketDto> {
    return this.http
      .post<ApiResponse<MaintenanceTicketDto>>(`${this.maintenanceBase}/tickets`, payload)
      .pipe(map((res) => res.data));
  }

  updateMaintenanceTicket(id: number, payload: MaintenanceTicketForm): Observable<MaintenanceTicketDto> {
    return this.http
      .put<ApiResponse<MaintenanceTicketDto>>(`${this.maintenanceBase}/tickets/${id}`, payload)
      .pipe(map((res) => res.data));
  }

  deleteMaintenanceTicket(id: number): Observable<void> {
    return this.http.delete<void>(`${this.maintenanceBase}/tickets/${id}`);
  }

  assignMaintenanceTicket(id: number, payload: AssignTechnicianForm, actor: string): Observable<MaintenanceTicketDto> {
    return this.http
      .post<ApiResponse<MaintenanceTicketDto>>(`${this.maintenanceBase}/tickets/${id}/assign`, payload, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  startMaintenanceTicket(id: number, actor: string): Observable<MaintenanceTicketDto> {
    return this.http
      .post<ApiResponse<MaintenanceTicketDto>>(`${this.maintenanceBase}/tickets/${id}/start`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  completeMaintenanceTicket(id: number, actor: string): Observable<MaintenanceTicketDto> {
    return this.http
      .post<ApiResponse<MaintenanceTicketDto>>(`${this.maintenanceBase}/tickets/${id}/complete`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  cancelMaintenanceTicket(id: number, actor: string): Observable<MaintenanceTicketDto> {
    return this.http
      .post<ApiResponse<MaintenanceTicketDto>>(`${this.maintenanceBase}/tickets/${id}/cancel`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  addMaintenanceChecklistItem(ticketId: number, payload: MaintenanceChecklistForm): Observable<MaintenanceChecklistDto> {
    return this.http
      .post<ApiResponse<MaintenanceChecklistDto>>(`${this.maintenanceBase}/tickets/${ticketId}/checklists`, payload)
      .pipe(map((res) => res.data));
  }

  addMaintenanceSparePart(ticketId: number, payload: MaintenanceSparePartForm): Observable<MaintenanceSparePartDto> {
    return this.http
      .post<ApiResponse<MaintenanceSparePartDto>>(`${this.maintenanceBase}/tickets/${ticketId}/spare-parts`, payload)
      .pipe(map((res) => res.data));
  }

  issueMaintenanceSparePart(ticketId: number, sparePartId: number, actor: string): Observable<MaintenanceSparePartDto> {
    return this.http
      .post<ApiResponse<MaintenanceSparePartDto>>(`${this.maintenanceBase}/tickets/${ticketId}/spare-parts/${sparePartId}/issue`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  // Inventory CRUD
  getProduct(id: number): Observable<ProductDto> {
    return this.http.get<ApiResponse<ProductDto>>(`${this.inventoryBase}/products/${id}`).pipe(map((res) => res.data));
  }

  createProduct(payload: ProductForm): Observable<ProductDto> {
    return this.http.post<ApiResponse<ProductDto>>(`${this.inventoryBase}/products`, payload).pipe(map((res) => res.data));
  }

  updateProduct(id: number, payload: ProductForm): Observable<ProductDto> {
    return this.http.put<ApiResponse<ProductDto>>(`${this.inventoryBase}/products/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.inventoryBase}/products/${id}`);
  }

  activateProduct(id: number): Observable<void> {
    return this.http.put<void>(`${this.inventoryBase}/products/${id}/activate`, {});
  }

  deactivateProduct(id: number): Observable<void> {
    return this.http.put<void>(`${this.inventoryBase}/products/${id}/deactivate`, {});
  }

  getCategory(id: number): Observable<ProductCategoryDto> {
    return this.http.get<ApiResponse<ProductCategoryDto>>(`${this.inventoryBase}/categories/${id}`).pipe(map((res) => res.data));
  }

  createCategory(payload: ProductCategoryForm): Observable<ProductCategoryDto> {
    return this.http.post<ApiResponse<ProductCategoryDto>>(`${this.inventoryBase}/categories`, payload).pipe(map((res) => res.data));
  }

  updateCategory(id: number, payload: ProductCategoryForm): Observable<ProductCategoryDto> {
    return this.http.put<ApiResponse<ProductCategoryDto>>(`${this.inventoryBase}/categories/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.inventoryBase}/categories/${id}`);
  }

  activateCategory(id: number): Observable<void> {
    return this.http.put<void>(`${this.inventoryBase}/categories/${id}/activate`, {});
  }

  deactivateCategory(id: number): Observable<void> {
    return this.http.put<void>(`${this.inventoryBase}/categories/${id}/deactivate`, {});
  }

  getWarehouse(id: number): Observable<WarehouseDto> {
    return this.http.get<ApiResponse<WarehouseDto>>(`${this.inventoryBase}/warehouses/${id}`).pipe(map((res) => res.data));
  }

  createWarehouse(payload: WarehouseForm): Observable<WarehouseDto> {
    return this.http.post<ApiResponse<WarehouseDto>>(`${this.inventoryBase}/warehouses`, payload).pipe(map((res) => res.data));
  }

  updateWarehouse(id: number, payload: WarehouseForm): Observable<WarehouseDto> {
    return this.http.put<ApiResponse<WarehouseDto>>(`${this.inventoryBase}/warehouses/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteWarehouse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.inventoryBase}/warehouses/${id}`);
  }

  activateWarehouse(id: number): Observable<void> {
    return this.http.put<void>(`${this.inventoryBase}/warehouses/${id}/activate`, {});
  }

  deactivateWarehouse(id: number): Observable<void> {
    return this.http.put<void>(`${this.inventoryBase}/warehouses/${id}/deactivate`, {});
  }

  getUnits(filters: Record<string, string | number | boolean> = {}): Observable<UnitOfMeasureDto[]> {
    return this.http
      .get<ApiResponse<UnitOfMeasureDto[]>>(`${this.inventoryBase}/units`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getUnit(id: number): Observable<UnitOfMeasureDto> {
    return this.http.get<ApiResponse<UnitOfMeasureDto>>(`${this.inventoryBase}/units/${id}`).pipe(map((res) => res.data));
  }

  createUnit(payload: UnitOfMeasureForm): Observable<UnitOfMeasureDto> {
    return this.http.post<ApiResponse<UnitOfMeasureDto>>(`${this.inventoryBase}/units`, payload).pipe(map((res) => res.data));
  }

  updateUnit(id: number, payload: UnitOfMeasureForm): Observable<UnitOfMeasureDto> {
    return this.http.put<ApiResponse<UnitOfMeasureDto>>(`${this.inventoryBase}/units/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteUnit(id: number): Observable<void> {
    return this.http.delete<void>(`${this.inventoryBase}/units/${id}`);
  }

  activateUnit(id: number): Observable<void> {
    return this.http.put<void>(`${this.inventoryBase}/units/${id}/activate`, {});
  }

  deactivateUnit(id: number): Observable<void> {
    return this.http.put<void>(`${this.inventoryBase}/units/${id}/deactivate`, {});
  }

  getStockMovement(id: number): Observable<StockMovementDto> {
    return this.http.get<ApiResponse<StockMovementDto>>(`${this.inventoryBase}/stock/movements/${id}`).pipe(map((res) => res.data));
  }

  createStockMovement(payload: StockMovementForm): Observable<StockMovementDto> {
    return this.http.post<ApiResponse<StockMovementDto>>(`${this.inventoryBase}/stock/movements`, payload).pipe(map((res) => res.data));
  }

  updateStockMovement(id: number, payload: StockMovementForm): Observable<StockMovementDto> {
    return this.http.put<ApiResponse<StockMovementDto>>(`${this.inventoryBase}/stock/movements/${id}`, payload).pipe(map((res) => res.data));
  }

  submitStockMovement(id: number): Observable<StockMovementDto> {
    return this.http.put<ApiResponse<StockMovementDto>>(`${this.inventoryBase}/stock/movements/${id}/submit`, {}).pipe(map((res) => res.data));
  }

  approveStockMovement(id: number): Observable<StockMovementDto> {
    return this.http.put<ApiResponse<StockMovementDto>>(`${this.inventoryBase}/stock/movements/${id}/approve`, {}).pipe(map((res) => res.data));
  }

  cancelStockMovement(id: number): Observable<StockMovementDto> {
    return this.http.put<ApiResponse<StockMovementDto>>(`${this.inventoryBase}/stock/movements/${id}/cancel`, {}).pipe(map((res) => res.data));
  }

  // Sales master
  getCustomer(id: number): Observable<CustomerDto> {
    return this.http.get<ApiResponse<CustomerDto>>(`${this.salesBase}/customers/${id}`).pipe(map((res) => res.data));
  }

  createCustomer(payload: CustomerForm): Observable<CustomerDto> {
    return this.http.post<ApiResponse<CustomerDto>>(`${this.salesBase}/customers`, payload).pipe(map((res) => res.data));
  }

  updateCustomer(id: number, payload: CustomerForm): Observable<CustomerDto> {
    return this.http.put<ApiResponse<CustomerDto>>(`${this.salesBase}/customers/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteCustomer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.salesBase}/customers/${id}`);
  }

  convertLeadToCustomer(leadId: number): Observable<CustomerDto> {
    return this.http.post<ApiResponse<CustomerDto>>(`${this.crmBase}/leads/${leadId}/convert`, {}).pipe(map((res) => res.data));
  }

  // Purchases master
  getSupplier(id: number): Observable<SupplierDto> {
    return this.http.get<ApiResponse<SupplierDto>>(`${this.purchasesBase}/suppliers/${id}`).pipe(map((res) => res.data));
  }

  createSupplier(payload: SupplierForm): Observable<SupplierDto> {
    return this.http.post<ApiResponse<SupplierDto>>(`${this.purchasesBase}/suppliers`, payload).pipe(map((res) => res.data));
  }

  updateSupplier(id: number, payload: SupplierForm): Observable<SupplierDto> {
    return this.http.put<ApiResponse<SupplierDto>>(`${this.purchasesBase}/suppliers/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteSupplier(id: number): Observable<void> {
    return this.http.delete<void>(`${this.purchasesBase}/suppliers/${id}`);
  }

  getSupplierPayment(id: number): Observable<SupplierPaymentDto> {
    return this.http.get<ApiResponse<SupplierPaymentDto>>(`${this.purchasesBase}/payments/${id}`).pipe(map((res) => res.data));
  }

  createSupplierPayment(payload: SupplierPaymentForm): Observable<SupplierPaymentDto> {
    return this.http.post<ApiResponse<SupplierPaymentDto>>(`${this.purchasesBase}/payments`, payload).pipe(map((res) => res.data));
  }

  updateSupplierPayment(id: number, payload: SupplierPaymentForm): Observable<SupplierPaymentDto> {
    return this.http.put<ApiResponse<SupplierPaymentDto>>(`${this.purchasesBase}/payments/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteSupplierPayment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.purchasesBase}/payments/${id}`);
  }

  approveSupplierPayment(id: number, actor: string): Observable<SupplierPaymentDto> {
    return this.http.post<ApiResponse<SupplierPaymentDto>>(`${this.purchasesBase}/payments/${id}/approve`, null, { params: { actor } }).pipe(map((res) => res.data));
  }

  cancelSupplierPayment(id: number, actor: string, reason?: string): Observable<SupplierPaymentDto> {
    return this.http.post<ApiResponse<SupplierPaymentDto>>(`${this.purchasesBase}/payments/${id}/cancel`, null, { params: this.toParams({ actor, reason }) }).pipe(map((res) => res.data));
  }

  // HR
  getDepartment(id: number): Observable<DepartmentDto> {
    return this.http.get<ApiResponse<DepartmentDto>>(`${this.hrBase}/departments/${id}`).pipe(map((res) => res.data));
  }

  createDepartment(payload: DepartmentForm): Observable<DepartmentDto> {
    return this.http.post<ApiResponse<DepartmentDto>>(`${this.hrBase}/departments`, payload).pipe(map((res) => res.data));
  }

  updateDepartment(id: number, payload: DepartmentForm): Observable<DepartmentDto> {
    return this.http.put<ApiResponse<DepartmentDto>>(`${this.hrBase}/departments/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteDepartment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.hrBase}/departments/${id}`);
  }

  getEmployee(id: number): Observable<EmployeeDto> {
    return this.http.get<ApiResponse<EmployeeDto>>(`${this.hrBase}/employees/${id}`).pipe(map((res) => res.data));
  }

  createEmployee(payload: EmployeeForm): Observable<EmployeeDto> {
    return this.http.post<ApiResponse<EmployeeDto>>(`${this.hrBase}/employees`, payload).pipe(map((res) => res.data));
  }

  updateEmployee(id: number, payload: EmployeeForm): Observable<EmployeeDto> {
    return this.http.put<ApiResponse<EmployeeDto>>(`${this.hrBase}/employees/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.hrBase}/employees/${id}`);
  }

  getAttendanceRecord(id: number): Observable<AttendanceRecordDto> {
    return this.http.get<ApiResponse<AttendanceRecordDto>>(`${this.hrBase}/attendance/${id}`).pipe(map((res) => res.data));
  }

  createAttendanceRecord(payload: AttendanceRecordForm): Observable<AttendanceRecordDto> {
    return this.http.post<ApiResponse<AttendanceRecordDto>>(`${this.hrBase}/attendance`, payload).pipe(map((res) => res.data));
  }

  updateAttendanceRecord(id: number, payload: AttendanceRecordForm): Observable<AttendanceRecordDto> {
    return this.http.put<ApiResponse<AttendanceRecordDto>>(`${this.hrBase}/attendance/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteAttendanceRecord(id: number): Observable<void> {
    return this.http.delete<void>(`${this.hrBase}/attendance/${id}`);
  }

  getLeaveRequest(id: number): Observable<LeaveRequestDto> {
    return this.http.get<ApiResponse<LeaveRequestDto>>(`${this.hrBase}/leave-requests/${id}`).pipe(map((res) => res.data));
  }

  createLeaveRequest(payload: LeaveRequestForm): Observable<LeaveRequestDto> {
    return this.http.post<ApiResponse<LeaveRequestDto>>(`${this.hrBase}/leave-requests`, payload).pipe(map((res) => res.data));
  }

  updateLeaveRequest(id: number, payload: LeaveRequestForm): Observable<LeaveRequestDto> {
    return this.http.put<ApiResponse<LeaveRequestDto>>(`${this.hrBase}/leave-requests/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteLeaveRequest(id: number): Observable<void> {
    return this.http.delete<void>(`${this.hrBase}/leave-requests/${id}`);
  }

  approveLeaveRequest(id: number, actor: string): Observable<LeaveRequestDto> {
    return this.http.post<ApiResponse<LeaveRequestDto>>(`${this.hrBase}/leave-requests/${id}/approve`, null, { params: { actor } }).pipe(map((res) => res.data));
  }

  cancelLeaveRequest(id: number, actor: string): Observable<LeaveRequestDto> {
    return this.http.post<ApiResponse<LeaveRequestDto>>(`${this.hrBase}/leave-requests/${id}/cancel`, null, { params: { actor } }).pipe(map((res) => res.data));
  }

  getPayrollRun(id: number): Observable<PayrollRunDto> {
    return this.http.get<ApiResponse<PayrollRunDto>>(`${this.hrBase}/payroll/${id}`).pipe(map((res) => res.data));
  }

  createPayrollRun(payload: PayrollRunForm): Observable<PayrollRunDto> {
    return this.http.post<ApiResponse<PayrollRunDto>>(`${this.hrBase}/payroll`, payload).pipe(map((res) => res.data));
  }

  updatePayrollRun(id: number, payload: PayrollRunForm): Observable<PayrollRunDto> {
    return this.http.put<ApiResponse<PayrollRunDto>>(`${this.hrBase}/payroll/${id}`, payload).pipe(map((res) => res.data));
  }

  deletePayrollRun(id: number): Observable<void> {
    return this.http.delete<void>(`${this.hrBase}/payroll/${id}`);
  }

  approvePayrollRun(id: number, actor: string): Observable<PayrollRunDto> {
    return this.http.post<ApiResponse<PayrollRunDto>>(`${this.hrBase}/payroll/${id}/approve`, null, { params: { actor } }).pipe(map((res) => res.data));
  }

  cancelPayrollRun(id: number, actor: string, reason?: string): Observable<PayrollRunDto> {
    return this.http.post<ApiResponse<PayrollRunDto>>(`${this.hrBase}/payroll/${id}/cancel`, null, { params: this.toParams({ actor, reason }) }).pipe(map((res) => res.data));
  }

  // CRM
  getLead(id: number): Observable<CrmLeadDto> {
    return this.http.get<ApiResponse<CrmLeadDto>>(`${this.crmBase}/leads/${id}`).pipe(map((res) => res.data));
  }

  createLead(payload: CrmLeadForm): Observable<CrmLeadDto> {
    return this.http.post<ApiResponse<CrmLeadDto>>(`${this.crmBase}/leads`, payload).pipe(map((res) => res.data));
  }

  updateLead(id: number, payload: CrmLeadForm): Observable<CrmLeadDto> {
    return this.http.put<ApiResponse<CrmLeadDto>>(`${this.crmBase}/leads/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteLead(id: number): Observable<void> {
    return this.http.delete<void>(`${this.crmBase}/leads/${id}`);
  }

  getCrmActivity(id: number): Observable<CrmActivityDto> {
    return this.http.get<ApiResponse<CrmActivityDto>>(`${this.crmBase}/activities/${id}`).pipe(map((res) => res.data));
  }

  createCrmActivity(payload: CrmActivityForm): Observable<CrmActivityDto> {
    return this.http.post<ApiResponse<CrmActivityDto>>(`${this.crmBase}/activities`, payload).pipe(map((res) => res.data));
  }

  updateCrmActivity(id: number, payload: CrmActivityForm): Observable<CrmActivityDto> {
    return this.http.put<ApiResponse<CrmActivityDto>>(`${this.crmBase}/activities/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteCrmActivity(id: number): Observable<void> {
    return this.http.delete<void>(`${this.crmBase}/activities/${id}`);
  }

  getCrmNotes(filters: Record<string, string | number | boolean> = {}): Observable<CrmNoteDto[]> {
    return this.http
      .get<ApiResponse<CrmNoteDto[]>>(`${this.crmBase}/notes`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getCrmNote(id: number): Observable<CrmNoteDto> {
    return this.http.get<ApiResponse<CrmNoteDto>>(`${this.crmBase}/notes/${id}`).pipe(map((res) => res.data));
  }

  createCrmNote(payload: CrmNoteForm): Observable<CrmNoteDto> {
    return this.http.post<ApiResponse<CrmNoteDto>>(`${this.crmBase}/notes`, payload).pipe(map((res) => res.data));
  }

  updateCrmNote(id: number, payload: CrmNoteForm): Observable<CrmNoteDto> {
    return this.http.put<ApiResponse<CrmNoteDto>>(`${this.crmBase}/notes/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteCrmNote(id: number): Observable<void> {
    return this.http.delete<void>(`${this.crmBase}/notes/${id}`);
  }

  // Projects
  getProject(id: number): Observable<ProjectDto> {
    return this.http.get<ApiResponse<ProjectDto>>(`${this.projectsBase}/${id}`).pipe(map((res) => res.data));
  }

  createProject(payload: ProjectForm): Observable<ProjectDto> {
    return this.http.post<ApiResponse<ProjectDto>>(`${this.projectsBase}`, payload).pipe(map((res) => res.data));
  }

  updateProject(id: number, payload: ProjectForm): Observable<ProjectDto> {
    return this.http.put<ApiResponse<ProjectDto>>(`${this.projectsBase}/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.projectsBase}/${id}`);
  }

  getProjectTasks(projectId: number): Observable<ProjectTaskDto[]> {
    return this.http
      .get<ApiResponse<ProjectTaskDto[]>>(`${this.projectsBase}/tasks`, { params: this.toParams({ projectId }) })
      .pipe(map((res) => res.data || []));
  }

  createProjectTask(payload: ProjectTaskForm): Observable<ProjectTaskDto> {
    return this.http.post<ApiResponse<ProjectTaskDto>>(`${this.projectsBase}/tasks`, payload).pipe(map((res) => res.data));
  }

  updateProjectTask(id: number, payload: ProjectTaskForm): Observable<ProjectTaskDto> {
    return this.http.put<ApiResponse<ProjectTaskDto>>(`${this.projectsBase}/tasks/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteProjectTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.projectsBase}/tasks/${id}`);
  }

  getProjectMembers(projectId: number): Observable<ProjectMemberDto[]> {
    return this.http
      .get<ApiResponse<ProjectMemberDto[]>>(`${this.projectsBase}/members`, { params: this.toParams({ projectId }) })
      .pipe(map((res) => res.data || []));
  }

  createProjectMember(payload: ProjectMemberForm): Observable<ProjectMemberDto> {
    return this.http.post<ApiResponse<ProjectMemberDto>>(`${this.projectsBase}/members`, payload).pipe(map((res) => res.data));
  }

  deleteProjectMember(id: number): Observable<void> {
    return this.http.delete<void>(`${this.projectsBase}/members/${id}`);
  }

  getProjectExpenses(projectId: number): Observable<ProjectExpenseDto[]> {
    return this.http
      .get<ApiResponse<ProjectExpenseDto[]>>(`${this.projectsBase}/expenses`, { params: this.toParams({ projectId }) })
      .pipe(map((res) => res.data || []));
  }

  createProjectExpense(payload: ProjectExpenseForm): Observable<ProjectExpenseDto> {
    return this.http.post<ApiResponse<ProjectExpenseDto>>(`${this.projectsBase}/expenses`, payload).pipe(map((res) => res.data));
  }

  deleteProjectExpense(id: number): Observable<void> {
    return this.http.delete<void>(`${this.projectsBase}/expenses/${id}`);
  }

  updateProjectExpense(id: number, payload: ProjectExpenseForm): Observable<ProjectExpenseDto> {
    return this.http.put<ApiResponse<ProjectExpenseDto>>(`${this.projectsBase}/expenses/${id}`, payload).pipe(map((res) => res.data));
  }

  approveProjectExpense(id: number, actor: string): Observable<ProjectExpenseDto> {
    return this.http.post<ApiResponse<ProjectExpenseDto>>(`${this.projectsBase}/expenses/${id}/approve`, null, { params: { actor } }).pipe(map((res) => res.data));
  }

  cancelProjectExpense(id: number, actor: string, reason?: string): Observable<ProjectExpenseDto> {
    const params: Record<string, string> = { actor };
    if (reason) params['reason'] = reason;
    return this.http.post<ApiResponse<ProjectExpenseDto>>(`${this.projectsBase}/expenses/${id}/cancel`, null, { params: this.toParams(params) }).pipe(map((res) => res.data));
  }

  // Payroll lines
  getPayrollLines(payrollId?: number): Observable<PayrollLineDto[]> {
    return this.http
      .get<ApiResponse<PayrollLineDto[]>>(`${this.hrBase}/payroll-lines`, { params: this.toParams({ payrollId }) })
      .pipe(map((res) => res.data || []));
  }

  getPayrollLine(id: number): Observable<PayrollLineDto> {
    return this.http.get<ApiResponse<PayrollLineDto>>(`${this.hrBase}/payroll-lines/${id}`).pipe(map((res) => res.data));
  }

  createPayrollLine(payload: PayrollLineForm): Observable<PayrollLineDto> {
    return this.http.post<ApiResponse<PayrollLineDto>>(`${this.hrBase}/payroll-lines`, payload).pipe(map((res) => res.data));
  }

  updatePayrollLine(id: number, payload: PayrollLineForm): Observable<PayrollLineDto> {
    return this.http.put<ApiResponse<PayrollLineDto>>(`${this.hrBase}/payroll-lines/${id}`, payload).pipe(map((res) => res.data));
  }

  deletePayrollLine(id: number): Observable<void> {
    return this.http.delete<void>(`${this.hrBase}/payroll-lines/${id}`);
  }

  // Employee documents
  getEmployeeDocuments(employeeId?: number): Observable<EmployeeDocumentDto[]> {
    return this.http
      .get<ApiResponse<EmployeeDocumentDto[]>>(`${this.hrBase}/documents`, { params: this.toParams({ employeeId }) })
      .pipe(map((res) => res.data || []));
  }

  getEmployeeDocument(id: number): Observable<EmployeeDocumentDto> {
    return this.http.get<ApiResponse<EmployeeDocumentDto>>(`${this.hrBase}/documents/${id}`).pipe(map((res) => res.data));
  }

  createEmployeeDocument(payload: EmployeeDocumentForm): Observable<EmployeeDocumentDto> {
    return this.http.post<ApiResponse<EmployeeDocumentDto>>(`${this.hrBase}/documents`, payload).pipe(map((res) => res.data));
  }

  updateEmployeeDocument(id: number, payload: EmployeeDocumentForm): Observable<EmployeeDocumentDto> {
    return this.http.put<ApiResponse<EmployeeDocumentDto>>(`${this.hrBase}/documents/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteEmployeeDocument(id: number): Observable<void> {
    return this.http.delete<void>(`${this.hrBase}/documents/${id}`);
  }

  // BOM
  getProductBomLines(parentProductId: number): Observable<ProductBomLineDto[]> {
    return this.http
      .get<ApiResponse<ProductBomLineDto[]>>(`${this.manufacturingBase}/bom`, { params: this.toParams({ parentProductId }) })
      .pipe(map((res) => res.data || []));
  }

  getProductBomLine(id: number): Observable<ProductBomLineDto> {
    return this.http.get<ApiResponse<ProductBomLineDto>>(`${this.manufacturingBase}/bom/${id}`).pipe(map((res) => res.data));
  }

  createProductBomLine(payload: ProductBomLineForm): Observable<ProductBomLineDto> {
    return this.http.post<ApiResponse<ProductBomLineDto>>(`${this.manufacturingBase}/bom`, payload).pipe(map((res) => res.data));
  }

  updateProductBomLine(id: number, payload: ProductBomLineForm): Observable<ProductBomLineDto> {
    return this.http.put<ApiResponse<ProductBomLineDto>>(`${this.manufacturingBase}/bom/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteProductBomLine(id: number): Observable<void> {
    return this.http.delete<void>(`${this.manufacturingBase}/bom/${id}`);
  }

  // Low stock
  getLowStockAlerts(): Observable<LowStockAlertDto[]> {
    return this.http
      .get<ApiResponse<LowStockAlertDto[]>>(`${this.inventoryBase}/stock/low-stock`)
      .pipe(map((res) => res.data || []));
  }

  // Stock incidents
  getStockIncidents(filters: Record<string, string> = {}): Observable<StockIncidentDto[]> {
    return this.http
      .get<ApiResponse<StockIncidentDto[]>>(`${this.inventoryBase}/stock/incidents`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getStockIncident(id: number): Observable<StockIncidentDto> {
    return this.http.get<ApiResponse<StockIncidentDto>>(`${this.inventoryBase}/stock/incidents/${id}`).pipe(map((res) => res.data));
  }

  createStockIncident(payload: StockIncidentForm): Observable<StockIncidentDto> {
    return this.http.post<ApiResponse<StockIncidentDto>>(`${this.inventoryBase}/stock/incidents`, payload).pipe(map((res) => res.data));
  }

  updateStockIncident(id: number, payload: StockIncidentForm): Observable<StockIncidentDto> {
    return this.http.put<ApiResponse<StockIncidentDto>>(`${this.inventoryBase}/stock/incidents/${id}`, payload).pipe(map((res) => res.data));
  }

  approveStockIncident(id: number): Observable<StockIncidentDto> {
    return this.http.post<ApiResponse<StockIncidentDto>>(`${this.inventoryBase}/stock/incidents/${id}/approve`, null).pipe(map((res) => res.data));
  }

  deleteStockIncident(id: number): Observable<void> {
    return this.http.delete<void>(`${this.inventoryBase}/stock/incidents/${id}`);
  }

  // Replenishment
  getReplenishmentProposals(filters: Record<string, string> = {}): Observable<ReplenishmentProposalDto[]> {
    return this.http
      .get<ApiResponse<ReplenishmentProposalDto[]>>(`${this.inventoryBase}/stock/replenishment`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  generateReplenishmentProposals(): Observable<ReplenishmentProposalDto[]> {
    return this.http
      .post<ApiResponse<ReplenishmentProposalDto[]>>(`${this.inventoryBase}/stock/replenishment/generate`, null)
      .pipe(map((res) => res.data || []));
  }

  convertReplenishmentToPurchaseOrder(supplierId: number, warehouseId?: number): Observable<PurchaseOrderDto> {
    return this.http
      .post<ApiResponse<PurchaseOrderDto>>(`${this.inventoryBase}/stock/replenishment/convert`, null, {
        params: this.toParams({ supplierId, warehouseId })
      })
      .pipe(map((res) => res.data));
  }

  // Labels
  getLabelPreview(productId: number): Observable<LabelPreviewDto> {
    return this.http
      .get<ApiResponse<LabelPreviewDto>>(`${this.inventoryBase}/labels/preview`, { params: { productId: String(productId) } })
      .pipe(map((res) => res.data));
  }

  // Purchase RFQs
  getPurchaseRfqs(): Observable<PurchaseRfqDto[]> {
    return this.http.get<ApiResponse<PurchaseRfqDto[]>>(`${this.purchasesBase}/rfqs`).pipe(map((res) => res.data || []));
  }

  getPurchaseRfq(id: number): Observable<PurchaseRfqDto> {
    return this.http.get<ApiResponse<PurchaseRfqDto>>(`${this.purchasesBase}/rfqs/${id}`).pipe(map((res) => res.data));
  }

  createPurchaseRfq(payload: PurchaseRfqForm): Observable<PurchaseRfqDto> {
    return this.http.post<ApiResponse<PurchaseRfqDto>>(`${this.purchasesBase}/rfqs`, payload).pipe(map((res) => res.data));
  }

  updatePurchaseRfq(id: number, payload: PurchaseRfqForm): Observable<PurchaseRfqDto> {
    return this.http.put<ApiResponse<PurchaseRfqDto>>(`${this.purchasesBase}/rfqs/${id}`, payload).pipe(map((res) => res.data));
  }

  submitPurchaseRfq(id: number): Observable<PurchaseRfqDto> {
    return this.http.post<ApiResponse<PurchaseRfqDto>>(`${this.purchasesBase}/rfqs/${id}/submit`, null).pipe(map((res) => res.data));
  }

  deletePurchaseRfq(id: number): Observable<void> {
    return this.http.delete<void>(`${this.purchasesBase}/rfqs/${id}`);
  }

  // Goods receipts
  getGoodsReceipts(): Observable<GoodsReceiptDto[]> {
    return this.http.get<ApiResponse<GoodsReceiptDto[]>>(`${this.purchasesBase}/receipts`).pipe(map((res) => res.data || []));
  }

  getGoodsReceipt(id: number): Observable<GoodsReceiptDto> {
    return this.http.get<ApiResponse<GoodsReceiptDto>>(`${this.purchasesBase}/receipts/${id}`).pipe(map((res) => res.data));
  }

  createGoodsReceipt(payload: GoodsReceiptForm): Observable<GoodsReceiptDto> {
    return this.http.post<ApiResponse<GoodsReceiptDto>>(`${this.purchasesBase}/receipts`, payload).pipe(map((res) => res.data));
  }

  updateGoodsReceipt(id: number, payload: GoodsReceiptForm): Observable<GoodsReceiptDto> {
    return this.http.put<ApiResponse<GoodsReceiptDto>>(`${this.purchasesBase}/receipts/${id}`, payload).pipe(map((res) => res.data));
  }

  approveGoodsReceipt(id: number): Observable<GoodsReceiptDto> {
    return this.http.post<ApiResponse<GoodsReceiptDto>>(`${this.purchasesBase}/receipts/${id}/approve`, null).pipe(map((res) => res.data));
  }

  deleteGoodsReceipt(id: number): Observable<void> {
    return this.http.delete<void>(`${this.purchasesBase}/receipts/${id}`);
  }

  // Document conversions
  convertQuotationToOrder(id: number, actor: string): Observable<SalesOrderDto> {
    return this.http
      .post<ApiResponse<SalesOrderDto>>(`${this.salesBase}/quotations/${id}/convert-to-order`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  convertSalesOrderToInvoice(id: number, actor: string): Observable<SalesInvoiceDto> {
    return this.http
      .post<ApiResponse<SalesInvoiceDto>>(`${this.salesBase}/orders/${id}/convert-to-invoice`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  convertPurchaseOrderToInvoice(id: number, actor: string): Observable<PurchaseInvoiceDto> {
    return this.http
      .post<ApiResponse<PurchaseInvoiceDto>>(`${this.purchasesBase}/orders/${id}/convert-to-invoice`, null, { params: { actor } })
      .pipe(map((res) => res.data));
  }

  generatePayrollFromAttendance(id: number): Observable<PayrollRunDto> {
    return this.http
      .post<ApiResponse<PayrollRunDto>>(`${this.hrBase}/payroll/${id}/generate-from-attendance`, null)
      .pipe(map((res) => res.data));
  }

  // Recruitment
  getVacancies(): Observable<HrVacancyDto[]> {
    return this.http.get<ApiResponse<HrVacancyDto[]>>(`${this.hrBase}/recruitment/vacancies`).pipe(map((res) => res.data || []));
  }

  createVacancy(payload: HrVacancyForm): Observable<HrVacancyDto> {
    return this.http.post<ApiResponse<HrVacancyDto>>(`${this.hrBase}/recruitment/vacancies`, payload).pipe(map((res) => res.data));
  }

  updateVacancy(id: number, payload: HrVacancyForm): Observable<HrVacancyDto> {
    return this.http.put<ApiResponse<HrVacancyDto>>(`${this.hrBase}/recruitment/vacancies/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteVacancy(id: number): Observable<void> {
    return this.http.delete<void>(`${this.hrBase}/recruitment/vacancies/${id}`);
  }

  getCandidates(vacancyId?: number): Observable<HrCandidateDto[]> {
    return this.http
      .get<ApiResponse<HrCandidateDto[]>>(`${this.hrBase}/recruitment/candidates`, { params: this.toParams({ vacancyId }) })
      .pipe(map((res) => res.data || []));
  }

  createCandidate(payload: HrCandidateForm): Observable<HrCandidateDto> {
    return this.http.post<ApiResponse<HrCandidateDto>>(`${this.hrBase}/recruitment/candidates`, payload).pipe(map((res) => res.data));
  }

  updateCandidate(id: number, payload: HrCandidateForm): Observable<HrCandidateDto> {
    return this.http.put<ApiResponse<HrCandidateDto>>(`${this.hrBase}/recruitment/candidates/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteCandidate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.hrBase}/recruitment/candidates/${id}`);
  }

  getInterviews(candidateId?: number): Observable<HrInterviewDto[]> {
    return this.http
      .get<ApiResponse<HrInterviewDto[]>>(`${this.hrBase}/recruitment/interviews`, { params: this.toParams({ candidateId }) })
      .pipe(map((res) => res.data || []));
  }

  createInterview(payload: HrInterviewForm): Observable<HrInterviewDto> {
    return this.http.post<ApiResponse<HrInterviewDto>>(`${this.hrBase}/recruitment/interviews`, payload).pipe(map((res) => res.data));
  }

  updateInterview(id: number, payload: HrInterviewForm): Observable<HrInterviewDto> {
    return this.http.put<ApiResponse<HrInterviewDto>>(`${this.hrBase}/recruitment/interviews/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteInterview(id: number): Observable<void> {
    return this.http.delete<void>(`${this.hrBase}/recruitment/interviews/${id}`);
  }

  getLeaveBalances(employeeId?: number, year?: number): Observable<LeaveBalanceDto[]> {
    return this.http
      .get<ApiResponse<LeaveBalanceDto[]>>(`${this.hrBase}/leave-balances`, { params: this.toParams({ employeeId, year }) })
      .pipe(map((res) => res.data || []));
  }

  // PMO
  getPmoMilestones(projectId: number): Observable<PmoMilestoneDto[]> {
    return this.http.get<ApiResponse<PmoMilestoneDto[]>>(`${this.pmoBase}/projects/${projectId}/milestones`).pipe(map((res) => res.data || []));
  }

  createPmoMilestone(projectId: number, payload: PmoMilestoneForm): Observable<PmoMilestoneDto> {
    return this.http.post<ApiResponse<PmoMilestoneDto>>(`${this.pmoBase}/projects/${projectId}/milestones`, payload).pipe(map((res) => res.data));
  }

  updatePmoMilestone(projectId: number, id: number, payload: PmoMilestoneForm): Observable<PmoMilestoneDto> {
    return this.http.put<ApiResponse<PmoMilestoneDto>>(`${this.pmoBase}/projects/${projectId}/milestones/${id}`, payload).pipe(map((res) => res.data));
  }

  deletePmoMilestone(projectId: number, id: number): Observable<void> {
    return this.http.delete<void>(`${this.pmoBase}/projects/${projectId}/milestones/${id}`);
  }

  getPmoRisks(projectId: number): Observable<PmoRiskDto[]> {
    return this.http.get<ApiResponse<PmoRiskDto[]>>(`${this.pmoBase}/projects/${projectId}/risks`).pipe(map((res) => res.data || []));
  }

  createPmoRisk(projectId: number, payload: PmoRiskForm): Observable<PmoRiskDto> {
    return this.http.post<ApiResponse<PmoRiskDto>>(`${this.pmoBase}/projects/${projectId}/risks`, payload).pipe(map((res) => res.data));
  }

  updatePmoRisk(projectId: number, id: number, payload: PmoRiskForm): Observable<PmoRiskDto> {
    return this.http.put<ApiResponse<PmoRiskDto>>(`${this.pmoBase}/projects/${projectId}/risks/${id}`, payload).pipe(map((res) => res.data));
  }

  deletePmoRisk(projectId: number, id: number): Observable<void> {
    return this.http.delete<void>(`${this.pmoBase}/projects/${projectId}/risks/${id}`);
  }

  getPmoIssues(projectId: number): Observable<PmoIssueDto[]> {
    return this.http.get<ApiResponse<PmoIssueDto[]>>(`${this.pmoBase}/projects/${projectId}/issues`).pipe(map((res) => res.data || []));
  }

  createPmoIssue(projectId: number, payload: PmoIssueForm): Observable<PmoIssueDto> {
    return this.http.post<ApiResponse<PmoIssueDto>>(`${this.pmoBase}/projects/${projectId}/issues`, payload).pipe(map((res) => res.data));
  }

  updatePmoIssue(projectId: number, id: number, payload: PmoIssueForm): Observable<PmoIssueDto> {
    return this.http.put<ApiResponse<PmoIssueDto>>(`${this.pmoBase}/projects/${projectId}/issues/${id}`, payload).pipe(map((res) => res.data));
  }

  deletePmoIssue(projectId: number, id: number): Observable<void> {
    return this.http.delete<void>(`${this.pmoBase}/projects/${projectId}/issues/${id}`);
  }

  // Digital literacy
  getDigitalCourses(): Observable<DigitalCourseDto[]> {
    return this.http.get<ApiResponse<DigitalCourseDto[]>>(`${this.digitalLiteracyBase}/courses`).pipe(map((res) => res.data || []));
  }

  createDigitalCourse(payload: DigitalCourseForm): Observable<DigitalCourseDto> {
    return this.http.post<ApiResponse<DigitalCourseDto>>(`${this.digitalLiteracyBase}/courses`, payload).pipe(map((res) => res.data));
  }

  updateDigitalCourse(id: number, payload: DigitalCourseForm): Observable<DigitalCourseDto> {
    return this.http.put<ApiResponse<DigitalCourseDto>>(`${this.digitalLiteracyBase}/courses/${id}`, payload).pipe(map((res) => res.data));
  }

  deleteDigitalCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.digitalLiteracyBase}/courses/${id}`);
  }

  getDigitalEnrollments(courseId?: number, employeeId?: number): Observable<DigitalEnrollmentDto[]> {
    return this.http
      .get<ApiResponse<DigitalEnrollmentDto[]>>(`${this.digitalLiteracyBase}/enrollments`, { params: this.toParams({ courseId, employeeId }) })
      .pipe(map((res) => res.data || []));
  }

  createDigitalEnrollment(payload: DigitalEnrollmentForm): Observable<DigitalEnrollmentDto> {
    return this.http.post<ApiResponse<DigitalEnrollmentDto>>(`${this.digitalLiteracyBase}/enrollments`, payload).pipe(map((res) => res.data));
  }

  updateDigitalEnrollment(id: number, payload: DigitalEnrollmentForm): Observable<DigitalEnrollmentDto> {
    return this.http.put<ApiResponse<DigitalEnrollmentDto>>(`${this.digitalLiteracyBase}/enrollments/${id}`, payload).pipe(map((res) => res.data));
  }

  updateDigitalEnrollmentProgress(id: number, progressPct: number, score?: number): Observable<DigitalEnrollmentDto> {
    return this.http
      .post<ApiResponse<DigitalEnrollmentDto>>(`${this.digitalLiteracyBase}/enrollments/${id}/progress`, null, { params: this.toParams({ progressPct, score }) })
      .pipe(map((res) => res.data));
  }

  deleteDigitalEnrollment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.digitalLiteracyBase}/enrollments/${id}`);
  }

  // License & backups
  getCurrentLicense(): Observable<LicenseDto | null> {
    return this.http.get<ApiResponse<LicenseDto | null>>(`${this.adminBase}/license`).pipe(map((res) => res.data ?? null));
  }

  activateLicense(payload: LicenseActivateForm): Observable<LicenseDto> {
    return this.http.post<ApiResponse<LicenseDto>>(`${this.adminBase}/license/activate`, payload).pipe(map((res) => res.data));
  }

  getBackups(): Observable<BackupJobDto[]> {
    return this.http.get<ApiResponse<BackupJobDto[]>>(`${this.adminBase}/backups`).pipe(map((res) => res.data || []));
  }

  createBackup(): Observable<BackupJobDto> {
    return this.http.post<ApiResponse<BackupJobDto>>(`${this.adminBase}/backups`, null).pipe(map((res) => res.data));
  }

  getBackup(id: number): Observable<BackupJobDto> {
    return this.http.get<ApiResponse<BackupJobDto>>(`${this.adminBase}/backups/${id}`).pipe(map((res) => res.data));
  }

  downloadBackup(id: number): Observable<Blob> {
    return this.http.get(`${this.adminBase}/backups/${id}/download`, { responseType: 'blob' });
  }

  // Alerts
  getAlerts(status?: string): Observable<AlertEventDto[]> {
    return this.http.get<ApiResponse<AlertEventDto[]>>(`${this.alertsBase}`, { params: this.toParams({ status }) }).pipe(map((res) => res.data || []));
  }

  acknowledgeAlert(id: number): Observable<AlertEventDto> {
    return this.http.post<ApiResponse<AlertEventDto>>(`${this.alertsBase}/${id}/acknowledge`, null).pipe(map((res) => res.data));
  }

  private getPaged<T>(url: string, filters: PagedQuery): Observable<PagedResult<T>> {
    return this.http
      .get<ApiResponse<PagedResponse<T>>>(url, { params: this.toParams(filters) })
      .pipe(map((res) => res.data));
  }

  private toParams(filters: Record<string, string | number | boolean | undefined | null>): HttpParams {
    let params = new HttpParams();
    Object.keys(filters || {}).forEach((key: string) => {
      const value = filters[key];
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return params;
  }
}
