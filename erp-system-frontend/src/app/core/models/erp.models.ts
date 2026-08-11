export type ErpTransactionStatus = 'DRAFT' | 'APPROVED' | 'REVERSED' | 'CANCELLED';
export type StockMovementType = 'IN' | 'OUT' | 'TRANSFER' | 'ADJUSTMENT';
export type LeadStatus = 'NEW' | 'CONTACTED' | 'QUALIFIED' | 'LOST' | 'CONVERTED';
export type CrmActivityStatus = 'PLANNED' | 'COMPLETED' | 'CANCELLED';
export type ProjectStatus = 'PLANNED' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED';

export interface ProductDto {
  id: number;
  code: string;
  barcode?: string;
  name: string;
  nameEn?: string;
  nameAr?: string;
  categoryId?: number;
  categoryCode?: string;
  categoryName?: string;
  unitId?: number;
  unitCode?: string;
  unitName?: string;
  costPrice?: number;
  salePrice?: number;
  reorderLevel?: number;
  active: boolean;
  description?: string;
  totalQuantity?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductCategoryDto {
  id: number;
  code: string;
  name: string;
  nameEn?: string;
  nameAr?: string;
  parentId?: number | null;
  parentCode?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WarehouseDto {
  id: number;
  code: string;
  name: string;
  nameEn?: string;
  nameAr?: string;
  location?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface StockLevelDto {
  id: number;
  productId: number;
  productCode: string;
  productName: string;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  quantity: number;
  reservedQuantity?: number;
  availableQuantity?: number;
  costPrice?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface StockMovementDto {
  id: number;
  movementNumber: string;
  movementDate: string;
  movementType: StockMovementType | string;
  productId: number;
  productCode: string;
  productName: string;
  warehouseId: number;
  warehouseCode: string;
  warehouseName: string;
  targetWarehouseId?: number;
  targetWarehouseCode?: string;
  targetWarehouseName?: string;
  quantity: number;
  unitCost?: number;
  referenceType?: string;
  referenceId?: number;
  notes?: string;
  status: ErpTransactionStatus | string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CustomerDto {
  id: number;
  code: string;
  nameEn: string;
  nameAr?: string;
  email?: string;
  phone?: string;
  taxNumber?: string;
  address?: string;
  creditLimit?: number;
  receivableAccountId?: number;
  receivableAccountCode?: string;
  receivableAccountName?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ErpDocumentLineForm {
  productId: number;
  description?: string;
  quantity: number;
  unitPrice: number;
  discountPercent?: number;
  taxPercent?: number;
}

export interface ErpSimpleLineForm {
  productId: number;
  quantity: number;
  unitPrice: number;
}

export interface SalesQuotationDto {
  id: number;
  quotationNumber: string;
  quotationDate: string;
  validUntil?: string;
  customerId: number;
  customerCode?: string;
  customerName: string;
  status: ErpTransactionStatus | string;
  subtotal?: number;
  discountAmount?: number;
  taxAmount?: number;
  totalAmount: number;
  notes?: string;
  lines?: ErpDocumentLineForm[];
  createdAt?: string;
  updatedAt?: string;
}

export interface SalesQuotationForm {
  quotationNumber?: string;
  quotationDate: string;
  validUntil?: string;
  customerId: number;
  discountAmount?: number;
  notes?: string;
  lines: ErpDocumentLineForm[];
}

export interface SalesOrderDto {
  id: number;
  orderNumber: string;
  orderDate: string;
  customerId: number;
  customerCode?: string;
  customerName: string;
  quotationId?: number;
  quotationNumber?: string;
  warehouseId?: number;
  warehouseCode?: string;
  warehouseName?: string;
  status: ErpTransactionStatus | string;
  subtotal?: number;
  discountAmount?: number;
  taxAmount?: number;
  totalAmount: number;
  notes?: string;
  lines?: ErpDocumentLineForm[];
  createdAt?: string;
  updatedAt?: string;
}

export interface SalesOrderForm {
  orderNumber?: string;
  orderDate: string;
  customerId: number;
  quotationId?: number;
  warehouseId?: number;
  discountAmount?: number;
  notes?: string;
  lines: ErpDocumentLineForm[];
}

export interface SalesInvoiceDto {
  id: number;
  invoiceNumber: string;
  invoiceDate: string;
  dueDate?: string;
  customerId: number;
  customerCode?: string;
  customerName: string;
  orderId?: number;
  orderNumber?: string;
  warehouseId?: number;
  warehouseCode?: string;
  warehouseName?: string;
  status: ErpTransactionStatus | string;
  subtotal?: number;
  discountAmount?: number;
  taxAmount?: number;
  totalAmount: number;
  paidAmount?: number;
  remainingAmount?: number;
  notes?: string;
  lines?: SalesInvoiceLineDto[];
  createdAt?: string;
  updatedAt?: string;
}

export interface SalesInvoiceLineDto {
  id?: number;
  productId: number;
  productCode?: string;
  productName?: string;
  description?: string;
  quantity: number;
  unitPrice: number;
  discountPercent?: number;
  taxPercent?: number;
  lineTotal?: number;
}

export interface SalesInvoiceForm {
  invoiceNumber?: string;
  invoiceDate: string;
  dueDate: string;
  customerId: number;
  orderId?: number;
  warehouseId?: number;
  discountAmount?: number;
  notes?: string;
  lines: SalesInvoiceLineForm[];
}

export interface SalesInvoiceLineForm {
  productId: number;
  description?: string;
  quantity: number;
  unitPrice: number;
  discountPercent?: number;
  taxPercent?: number;
}

export interface SalesReturnDto {
  id: number;
  returnNumber: string;
  returnDate: string;
  customerId: number;
  customerCode?: string;
  customerName: string;
  invoiceId?: number;
  invoiceNumber?: string;
  warehouseId?: number;
  warehouseCode?: string;
  warehouseName?: string;
  status: ErpTransactionStatus | string;
  subtotal?: number;
  taxAmount?: number;
  totalAmount: number;
  notes?: string;
  lines?: ErpSimpleLineForm[];
  createdAt?: string;
  updatedAt?: string;
}

export interface SalesReturnForm {
  returnNumber?: string;
  returnDate: string;
  customerId: number;
  invoiceId?: number;
  warehouseId?: number;
  taxAmount?: number;
  notes?: string;
  lines: ErpSimpleLineForm[];
}

export interface SupplierDto {
  id: number;
  code: string;
  nameEn: string;
  nameAr?: string;
  email?: string;
  phone?: string;
  taxNumber?: string;
  address?: string;
  payableAccountId?: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface PurchaseOrderDto {
  id: number;
  orderNumber: string;
  orderDate: string;
  supplierId: number;
  supplierCode?: string;
  supplierName?: string;
  warehouseId?: number;
  warehouseCode?: string;
  warehouseName?: string;
  status: ErpTransactionStatus | string;
  subtotal?: number;
  discountAmount?: number;
  taxAmount?: number;
  totalAmount: number;
  notes?: string;
  lines?: ErpDocumentLineForm[];
  createdAt?: string;
  updatedAt?: string;
}

export interface PurchaseOrderForm {
  orderNumber?: string;
  orderDate: string;
  supplierId: number;
  warehouseId?: number;
  discountAmount?: number;
  notes?: string;
  lines: ErpDocumentLineForm[];
}

export interface PurchaseInvoiceDto {
  id: number;
  invoiceNumber: string;
  invoiceDate: string;
  dueDate?: string;
  supplierId: number;
  supplierCode?: string;
  supplierName?: string;
  orderId?: number;
  warehouseId?: number;
  warehouseCode?: string;
  warehouseName?: string;
  status: ErpTransactionStatus | string;
  subtotal?: number;
  discountAmount?: number;
  taxAmount?: number;
  totalAmount: number;
  paidAmount?: number;
  remainingAmount?: number;
  notes?: string;
  lines?: PurchaseInvoiceLineDto[];
  createdAt?: string;
  updatedAt?: string;
}

export interface PurchaseInvoiceLineDto {
  id?: number;
  productId: number;
  productCode?: string;
  productName?: string;
  description?: string;
  quantity: number;
  unitPrice: number;
  discountPercent?: number;
  taxPercent?: number;
  lineTotal?: number;
}

export interface PurchaseInvoiceForm {
  invoiceNumber?: string;
  invoiceDate: string;
  dueDate: string;
  supplierId: number;
  orderId?: number;
  warehouseId?: number;
  discountAmount?: number;
  notes?: string;
  lines: PurchaseInvoiceLineForm[];
}

export interface PurchaseInvoiceLineForm {
  productId: number;
  description?: string;
  quantity: number;
  unitPrice: number;
  discountPercent?: number;
  taxPercent?: number;
}

export type WorkOrderStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface WorkOrderDto {
  id: number;
  orderNumber: string;
  productId: number;
  productCode?: string;
  productName?: string;
  warehouseId?: number;
  warehouseName?: string;
  quantity: number;
  producedQuantity?: number;
  status: WorkOrderStatus | string;
  plannedStart?: string;
  plannedEnd?: string;
  notes?: string;
  startedAt?: string;
  completedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface WorkOrderForm {
  orderNumber?: string;
  productId: number;
  warehouseId?: number;
  quantity: number;
  plannedStart?: string;
  plannedEnd?: string;
  notes?: string;
}

export type MaintenanceAssetStatus = 'ACTIVE' | 'INACTIVE';
export type MaintenanceTicketStatus = 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'CLOSED' | 'CANCELLED';
export type MaintenanceTicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type MaintenanceTicketType = 'CORRECTIVE' | 'PREVENTIVE' | 'INSPECTION';

export interface MaintenanceAssetDto {
  id: number;
  assetCode: string;
  name: string;
  serialNo?: string;
  customerId?: number;
  customerName?: string;
  status: MaintenanceAssetStatus | string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface MaintenanceAssetForm {
  assetCode: string;
  name: string;
  serialNo?: string;
  customerId?: number;
  status?: string;
  notes?: string;
}

export interface MaintenanceTechnicianDto {
  id: number;
  employeeId?: number;
  employeeName?: string;
  displayName: string;
  skillsCsv?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface MaintenanceTechnicianForm {
  employeeId?: number;
  displayName: string;
  skillsCsv?: string;
  active?: boolean;
}

export interface MaintenanceChecklistDto {
  id?: number;
  ticketId?: number;
  itemText: string;
  done?: boolean;
  sortOrder?: number;
}

export interface MaintenanceChecklistForm {
  id?: number;
  itemText: string;
  done?: boolean;
  sortOrder?: number;
}

export interface MaintenanceSparePartDto {
  id: number;
  ticketId: number;
  productId: number;
  productCode?: string;
  productName?: string;
  warehouseId: number;
  warehouseName?: string;
  quantity: number;
  unitCost?: number;
  movementId?: number;
  issued?: boolean;
}

export interface MaintenanceSparePartForm {
  productId: number;
  warehouseId: number;
  quantity: number;
  unitCost?: number;
}

export interface MaintenanceTicketDto {
  id: number;
  ticketNo: string;
  assetId?: number;
  assetCode?: string;
  assetName?: string;
  customerId?: number;
  customerName?: string;
  title: string;
  description?: string;
  priority: MaintenanceTicketPriority | string;
  status: MaintenanceTicketStatus | string;
  ticketType: MaintenanceTicketType | string;
  technicianId?: number;
  technicianName?: string;
  slaHours?: number;
  openedAt?: string;
  closedAt?: string;
  checklists?: MaintenanceChecklistDto[];
  spareParts?: MaintenanceSparePartDto[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface MaintenanceTicketForm {
  ticketNo?: string;
  assetId?: number;
  customerId?: number;
  title: string;
  description?: string;
  priority?: string;
  ticketType?: string;
  technicianId?: number;
  slaHours?: number;
  checklists?: MaintenanceChecklistForm[];
}

export interface AssignTechnicianForm {
  technicianId: number;
}

export interface PurchaseReturnDto {
  id: number;
  returnNumber: string;
  returnDate: string;
  supplierId: number;
  supplierCode?: string;
  supplierName?: string;
  invoiceId?: number;
  warehouseId?: number;
  warehouseCode?: string;
  warehouseName?: string;
  status: ErpTransactionStatus | string;
  subtotal?: number;
  taxAmount?: number;
  totalAmount: number;
  notes?: string;
  lines?: ErpSimpleLineForm[];
  createdAt?: string;
  updatedAt?: string;
}

export interface PurchaseReturnForm {
  returnNumber?: string;
  returnDate: string;
  supplierId: number;
  invoiceId?: number;
  warehouseId?: number;
  taxAmount?: number;
  notes?: string;
  lines: ErpSimpleLineForm[];
}

export interface SupplierPaymentDto {
  id: number;
  paymentNumber: string;
  paymentDate: string;
  supplierId: number;
  invoiceId?: number;
  amount: number;
  paymentMethod?: string;
  status: ErpTransactionStatus | string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface DepartmentDto {
  id: number;
  code: string;
  nameEn: string;
  nameAr?: string;
  managerId?: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface EmployeeDto {
  id: number;
  employeeCode: string;
  fullNameEn: string;
  fullNameAr?: string;
  email?: string;
  phone?: string;
  departmentId?: number;
  jobTitle?: string;
  hireDate?: string;
  basicSalary?: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface AttendanceRecordDto {
  id: number;
  employeeId: number;
  attendanceDate: string;
  checkIn?: string;
  checkOut?: string;
  status?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface LeaveRequestDto {
  id: number;
  employeeId: number;
  leaveType: string;
  startDate: string;
  endDate: string;
  status: ErpTransactionStatus | string;
  reason?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PayrollRunDto {
  id: number;
  payrollNumber: string;
  periodStart: string;
  periodEnd: string;
  status: ErpTransactionStatus | string;
  totalAmount: number;
  notes?: string;
  journalEntryId?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PayrollLineDto {
  id: number;
  payrollId: number;
  employeeId: number;
  basicSalary: number;
  allowances?: number;
  deductions?: number;
  netSalary: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PayrollLineForm {
  payrollId: number;
  employeeId: number;
  basicSalary: number;
  allowances?: number;
  deductions?: number;
  netSalary: number;
}

export interface EmployeeDocumentDto {
  id: number;
  employeeId: number;
  documentType: string;
  fileName: string;
  filePath?: string;
  expiryDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface EmployeeDocumentForm {
  employeeId: number;
  documentType: string;
  fileName: string;
  filePath?: string;
  expiryDate?: string;
}

export interface ProductBomLineDto {
  id: number;
  parentProductId: number;
  parentProductCode?: string;
  parentProductName?: string;
  componentProductId: number;
  componentProductCode?: string;
  componentProductName?: string;
  quantityPerUnit: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductBomLineForm {
  parentProductId: number;
  componentProductId: number;
  quantityPerUnit: number;
}

export interface LowStockAlertDto {
  productId: number;
  productCode?: string;
  productName?: string;
  reorderLevel: number;
  totalQuantity: number;
  shortfall: number;
}

export interface CrmLeadDto {
  id: number;
  leadNumber: string;
  name: string;
  company?: string;
  email?: string;
  phone?: string;
  source?: string;
  status: LeadStatus | string;
  customerId?: number;
  assignedTo?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CrmActivityDto {
  id: number;
  activityType: string;
  subject: string;
  customerId?: number;
  leadId?: number;
  activityDate: string;
  status: CrmActivityStatus | string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectDto {
  id: number;
  projectCode: string;
  nameEn: string;
  nameAr?: string;
  customerId?: number;
  startDate?: string;
  endDate?: string;
  budget?: number;
  status: ProjectStatus | string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ErpReportInvoiceLine {
  number: string;
  date: string;
  customer?: string;
  supplier?: string;
  total: number;
}

export interface ErpSalesReportDto {
  fromDate: string;
  toDate: string;
  invoiceCount: number;
  totalSales: number;
  invoices: ErpReportInvoiceLine[];
}

export interface ErpPurchasesReportDto {
  fromDate: string;
  toDate: string;
  invoiceCount: number;
  totalPurchases: number;
  invoices: ErpReportInvoiceLine[];
}

export interface ErpInventoryReportDto {
  totalSkus: number;
  lowStockCount: number;
  totalQuantity: number;
  totalValuation?: number;
  stockLevels: StockLevelDto[];
  lowStockAlerts: Array<{ productId: number; productCode: string; productName: string; quantity: number; reorderLevel: number }>;
}

export interface ErpProfitReportDto {
  fromDate: string;
  toDate: string;
  totalSales: number;
  totalPurchases: number;
  netProfit: number;
}

export interface ActivityLogDto {
  id: number;
  moduleName: string;
  actionType: string;
  entityType: string;
  entityId?: number;
  entityReference?: string;
  description?: string;
  actor?: string;
  createdAt?: string;
}

export interface ErpDashboardDto {
  totalSales: number;
  totalPurchases: number;
  netProfit: number;
  newOrders: number;
  salesGrowthPercent?: number;
  purchasesGrowthPercent?: number;
  profitGrowthPercent?: number;
  lowStockCount: number;
  monthlySales?: Array<{ month: string; amount: number }>;
  monthlyExpenses?: Array<{ month: string; amount: number }>;
  topProducts?: Array<{ productId: number; productCode: string; productName: string; quantitySold: number; totalRevenue: number }>;
  lowStockItems?: Array<{ productId: number; productCode: string; productName: string; quantity: number; reorderLevel: number }>;
  recentActivities?: ActivityLogDto[];
  revenueByDepartment?: Array<{ departmentName: string; amount: number; percent: number }>;
  employeePerformance?: Array<{ employeeId: number; employeeName: string; salesAmount: number; performancePercent: number }>;
  ordersGrowthPercent?: number;
}

export interface UnitOfMeasureDto {
  id: number;
  code: string;
  nameEn: string;
  nameAr?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductForm {
  code: string;
  barcode?: string;
  nameEn: string;
  nameAr?: string;
  categoryId?: number;
  unitId: number;
  costPrice?: number;
  salePrice?: number;
  reorderLevel?: number;
  active?: boolean;
  description?: string;
}

export interface ProductCategoryForm {
  code: string;
  nameEn: string;
  nameAr?: string;
  parentId?: number | null;
  active?: boolean;
}

export interface WarehouseForm {
  code: string;
  nameEn: string;
  nameAr?: string;
  location?: string;
  active?: boolean;
}

export interface UnitOfMeasureForm {
  code: string;
  nameEn: string;
  nameAr?: string;
  active?: boolean;
}

export interface StockMovementForm {
  movementNumber?: string;
  movementDate: string;
  movementType: StockMovementType | string;
  productId: number;
  warehouseId: number;
  targetWarehouseId?: number;
  quantity: number;
  unitCost?: number;
  referenceType?: string;
  referenceId?: number;
  notes?: string;
  approveImmediately?: boolean;
}

export interface CustomerForm {
  code?: string;
  nameEn: string;
  nameAr?: string;
  email?: string;
  phone?: string;
  taxNumber?: string;
  address?: string;
  creditLimit?: number;
  receivableAccountId?: number;
  active?: boolean;
}

export interface SupplierForm {
  code?: string;
  nameEn: string;
  nameAr?: string;
  email?: string;
  phone?: string;
  taxNumber?: string;
  address?: string;
  payableAccountId?: number;
  active?: boolean;
}

export interface SupplierPaymentForm {
  paymentNumber?: string;
  paymentDate: string;
  supplierId: number;
  invoiceId?: number;
  amount: number;
  paymentMethod: string;
  notes?: string;
}

export interface DepartmentForm {
  code: string;
  nameEn: string;
  nameAr?: string;
  managerId?: number;
  active?: boolean;
}

export interface EmployeeForm {
  employeeCode: string;
  fullNameEn: string;
  fullNameAr?: string;
  email?: string;
  phone?: string;
  departmentId?: number;
  jobTitle?: string;
  hireDate?: string;
  basicSalary: number;
  active?: boolean;
}

export interface AttendanceRecordForm {
  employeeId: number;
  attendanceDate: string;
  checkIn?: string;
  checkOut?: string;
  status?: string;
  notes?: string;
}

export interface LeaveRequestForm {
  employeeId: number;
  leaveType: string;
  startDate: string;
  endDate: string;
  reason?: string;
}

export interface PayrollRunForm {
  payrollNumber?: string;
  periodStart: string;
  periodEnd: string;
  totalAmount: number;
  notes?: string;
}

export interface CrmLeadForm {
  leadNumber: string;
  name: string;
  company?: string;
  email?: string;
  phone?: string;
  source?: string;
  status: LeadStatus | string;
  customerId?: number;
  assignedTo?: string;
  notes?: string;
}

export interface CrmActivityForm {
  activityType: string;
  subject: string;
  customerId?: number;
  leadId?: number;
  activityDate: string;
  status: CrmActivityStatus | string;
  notes?: string;
}

export interface CrmNoteDto {
  id: number;
  entityType: string;
  entityId: number;
  content: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CrmNoteForm {
  entityType: string;
  entityId: number;
  content: string;
}

export interface ProjectForm {
  projectCode: string;
  nameEn: string;
  nameAr?: string;
  customerId?: number;
  startDate?: string;
  endDate?: string;
  budget: number;
  status: ProjectStatus | string;
  description?: string;
}

export interface ProjectTaskDto {
  id: number;
  projectId: number;
  title: string;
  description?: string;
  assignedTo?: string;
  dueDate?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectTaskForm {
  projectId: number;
  title: string;
  description?: string;
  assignedTo?: string;
  dueDate?: string;
  status?: string;
}

export interface ProjectMemberDto {
  id: number;
  projectId: number;
  employeeId?: number;
  role?: string;
  createdAt?: string;
}

export interface ProjectMemberForm {
  projectId: number;
  employeeId?: number;
  role?: string;
}

export interface ProjectExpenseDto {
  id: number;
  projectId: number;
  expenseDate: string;
  description?: string;
  amount: number;
  status?: string;
  journalEntryId?: number;
  createdAt?: string;
}

export interface ProjectExpenseForm {
  projectId: number;
  expenseDate: string;
  description?: string;
  amount: number;
}

export interface PosTerminalDto {
  id: number;
  code: string;
  name: string;
  warehouseId: number;
  warehouseCode?: string;
  warehouseName?: string;
  active?: boolean;
}

export interface PosShiftDto {
  id: number;
  shiftNo: string;
  terminalId: number;
  terminalCode?: string;
  terminalName?: string;
  cashierUserId: number;
  cashierUsername?: string;
  warehouseId: number;
  warehouseCode?: string;
  warehouseName?: string;
  status: string;
  openingCash: number;
  closingCash?: number;
  expectedCash?: number;
  cashSales?: number;
  cardSales?: number;
  creditSales?: number;
  discrepancy?: number;
  notes?: string;
  openedAt?: string;
  closedAt?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface PosShiftOpenForm {
  terminalId: number;
  warehouseId: number;
  openingCash: number;
  cashierUserId: number;
  notes?: string;
}

export interface PosShiftCloseForm {
  closingCash: number;
  notes?: string;
}

export interface PosSaleLineDto {
  id?: number;
  productId: number;
  productCode?: string;
  productName?: string;
  quantity: number;
  unitPrice: number;
  discountAmount?: number;
  taxRate?: number;
  lineTotal?: number;
}

export interface PosSaleDto {
  id: number;
  saleNo: string;
  shiftId: number;
  shiftNo?: string;
  customerId?: number;
  customerName?: string;
  warehouseId: number;
  warehouseCode?: string;
  status?: string;
  subtotal?: number;
  discountAmount?: number;
  taxAmount?: number;
  totalAmount: number;
  paymentMethod?: string;
  paidCash?: number;
  paidCard?: number;
  paidCredit?: number;
  idempotencyKey?: string;
  offlineBatchId?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  lines?: PosSaleLineDto[];
}

export interface PosSaleLineForm {
  productId: number;
  quantity: number;
  unitPrice: number;
  discountAmount?: number;
  taxRate?: number;
}

export interface PosSaleForm {
  shiftId: number;
  warehouseId: number;
  customerId?: number;
  discountAmount?: number;
  paidCash?: number;
  paidCard?: number;
  paidCredit?: number;
  idempotencyKey?: string;
  offlineBatchId?: string;
  lines: PosSaleLineForm[];
}

export interface PosOfflineSyncForm {
  batchKey: string;
  terminalId?: number;
  sales: PosSaleForm[];
}

export interface PosOfflineSyncResultDto {
  batchKey: string;
  status: string;
  processedCount: number;
  skippedCount: number;
  failedCount: number;
  sales: PosSaleDto[];
}


export interface LabelPreviewDto {
  productId: number;
  barcode: string;
  qrPayload: string;
  name: string;
  price: number;
}

export interface StockIncidentDto {
  id: number;
  incidentNo: string;
  warehouseId: number;
  warehouseCode?: string;
  warehouseName?: string;
  productId: number;
  productCode?: string;
  productName?: string;
  quantity: number;
  incidentType: string;
  reasonCode?: string;
  notes?: string;
  unitCost?: number;
  financialImpact?: number;
  status: string;
  approvedBy?: string;
  approvedAt?: string;
  movementId?: number;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface StockIncidentForm {
  incidentNo?: string;
  warehouseId: number;
  productId: number;
  quantity: number;
  incidentType: string;
  reasonCode?: string;
  notes?: string;
  unitCost?: number;
}

export interface ReplenishmentProposalDto {
  id: number;
  warehouseId: number;
  warehouseCode?: string;
  warehouseName?: string;
  productId: number;
  productCode?: string;
  productName?: string;
  currentQty: number;
  reorderLevel: number;
  proposedQty: number;
  status: string;
  purchaseOrderId?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PurchaseRfqDto {
  id: number;
  rfqNo: string;
  title: string;
  status: string;
  dueDate?: string;
  notes?: string;
  lines?: PurchaseRfqLineDto[];
  quotes?: PurchaseRfqQuoteDto[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface PurchaseRfqLineDto {
  id?: number;
  rfqId?: number;
  productId: number;
  productCode?: string;
  productName?: string;
  quantity: number;
  notes?: string;
}

export interface PurchaseRfqForm {
  rfqNo?: string;
  title: string;
  dueDate?: string;
  notes?: string;
  lines: PurchaseRfqLineDto[];
}

export interface PurchaseRfqQuoteDto {
  id: number;
  rfqId: number;
  supplierId: number;
  supplierName?: string;
  unitPrice: number;
  leadDays: number;
  notes?: string;
  selected: boolean;
  createdAt?: string;
}

export interface GoodsReceiptDto {
  id: number;
  receiptNo: string;
  supplierId?: number;
  supplierName?: string;
  warehouseId: number;
  warehouseCode?: string;
  warehouseName?: string;
  purchaseOrderId?: number;
  status: string;
  receivedAt?: string;
  notes?: string;
  lines?: GoodsReceiptLineDto[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface GoodsReceiptLineDto {
  id?: number;
  receiptId?: number;
  productId: number;
  productCode?: string;
  productName?: string;
  quantity: number;
  unitCost?: number;
}

export interface GoodsReceiptForm {
  receiptNo?: string;
  supplierId?: number;
  warehouseId: number;
  purchaseOrderId?: number;
  notes?: string;
  lines: GoodsReceiptLineDto[];
}

export interface ProductUomConversionDto {
  id: number;
  productId: number;
  unitId: number;
  unitCode?: string;
  unitName?: string;
  factorToBase: number;
  purchase: boolean;
  sales: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductBarcodeDto {
  id: number;
  productId: number;
  barcode: string;
  primaryBarcode: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface HrVacancyDto {
  id: number;
  title: string;
  departmentId?: number;
  status: string;
  openings: number;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface HrVacancyForm {
  title: string;
  departmentId?: number;
  status: string;
  openings: number;
  description?: string;
}

export interface HrCandidateDto {
  id: number;
  fullName: string;
  email?: string;
  phone?: string;
  vacancyId?: number;
  status: string;
  score?: number;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface HrCandidateForm {
  fullName: string;
  email?: string;
  phone?: string;
  vacancyId?: number;
  status: string;
  score?: number;
  notes?: string;
}

export interface HrInterviewDto {
  id: number;
  candidateId: number;
  scheduledAt: string;
  interviewer?: string;
  result?: string;
  notes?: string;
  createdAt?: string;
}

export interface HrInterviewForm {
  candidateId: number;
  scheduledAt: string;
  interviewer?: string;
  result?: string;
  notes?: string;
}

export interface LeaveBalanceDto {
  id: number;
  employeeId: number;
  leaveType: string;
  balanceDays: number;
  year: number;
}

export interface PmoMilestoneDto {
  id: number;
  projectId: number;
  title: string;
  dueDate?: string;
  status: string;
  sortOrder: number;
}

export interface PmoMilestoneForm {
  title: string;
  dueDate?: string;
  status: string;
  sortOrder?: number;
}

export interface PmoRiskDto {
  id: number;
  projectId: number;
  title: string;
  severity: string;
  status: string;
  mitigation?: string;
}

export interface PmoRiskForm {
  title: string;
  severity: string;
  status: string;
  mitigation?: string;
}

export interface PmoIssueDto {
  id: number;
  projectId: number;
  title: string;
  status: string;
  ownerName?: string;
  notes?: string;
}

export interface PmoIssueForm {
  title: string;
  status: string;
  ownerName?: string;
  notes?: string;
}

export interface DigitalCourseDto {
  id: number;
  code: string;
  title: string;
  description?: string;
  active: boolean;
  createdAt?: string;
}

export interface DigitalCourseForm {
  code: string;
  title: string;
  description?: string;
  active?: boolean;
}

export interface DigitalEnrollmentDto {
  id: number;
  courseId: number;
  employeeId: number;
  progressPct: number;
  score?: number;
  status: string;
  completedAt?: string;
  certificateNo?: string;
}

export interface DigitalEnrollmentForm {
  courseId: number;
  employeeId: number;
  progressPct?: number;
  score?: number;
  status?: string;
}

export interface LicenseDto {
  id: number;
  licenseKey: string;
  customerName: string;
  modulesCsv?: string;
  maxUsers: number;
  validFrom: string;
  validTo: string;
  graceDays: number;
  active: boolean;
  valid: boolean;
  activatedAt?: string;
}

export interface LicenseActivateForm {
  licenseKey: string;
  customerName: string;
  modulesCsv?: string;
  maxUsers?: number;
  validFrom: string;
  validTo: string;
  graceDays?: number;
  signature: string;
}

export interface BackupJobDto {
  id: number;
  jobNo: string;
  status: string;
  triggerType: string;
  filePath?: string;
  fileSizeBytes?: number;
  checksumSha256?: string;
  errorMessage?: string;
  startedAt?: string;
  finishedAt?: string;
  createdAt?: string;
  createdBy?: string;
  downloadable?: boolean;
}

export interface AlertEventDto {
  id: number;
  ruleId?: number;
  title: string;
  body?: string;
  severity: string;
  entityType?: string;
  entityRef?: string;
  deepLink?: string;
  status: string;
  createdAt?: string;
  acknowledgedAt?: string;
}
