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
  ProjectExpenseForm
} from '../models/erp.models';

@Injectable({ providedIn: 'root' })
export class ErpApiService {
  private readonly inventoryBase = `${environment.apiUrl}/inventory`;
  private readonly salesBase = `${environment.apiUrl}/sales`;
  private readonly purchasesBase = `${environment.apiUrl}/purchases`;
  private readonly hrBase = `${environment.apiUrl}/hr`;
  private readonly crmBase = `${environment.apiUrl}/crm`;
  private readonly projectsBase = `${environment.apiUrl}/projects`;
  private readonly manufacturingBase = `${environment.apiUrl}/manufacturing`;
  private readonly erpBase = `${environment.apiUrl}/erp`;

  constructor(private http: HttpClient) {}

  // Inventory
  getProducts(filters: Record<string, string | number | boolean> = {}): Observable<ProductDto[]> {
    return this.http
      .get<ApiResponse<ProductDto[]>>(`${this.inventoryBase}/products`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getCategories(filters: Record<string, string | number | boolean> = {}): Observable<ProductCategoryDto[]> {
    return this.http
      .get<ApiResponse<ProductCategoryDto[]>>(`${this.inventoryBase}/categories`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
  }

  getWarehouses(filters: Record<string, string | number | boolean> = {}): Observable<WarehouseDto[]> {
    return this.http
      .get<ApiResponse<WarehouseDto[]>>(`${this.inventoryBase}/warehouses`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
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

  // Sales
  getCustomers(filters: Record<string, string | number | boolean> = {}): Observable<CustomerDto[]> {
    return this.http
      .get<ApiResponse<CustomerDto[]>>(`${this.salesBase}/customers`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
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

  getPurchaseOrders(filters: Record<string, string | number | boolean> = {}): Observable<PurchaseOrderDto[]> {
    return this.http
      .get<ApiResponse<PurchaseOrderDto[]>>(`${this.purchasesBase}/orders`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
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

  getAttendanceRecords(filters: Record<string, string | number | boolean> = {}): Observable<AttendanceRecordDto[]> {
    return this.http
      .get<ApiResponse<AttendanceRecordDto[]>>(`${this.hrBase}/attendance`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data || []));
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
