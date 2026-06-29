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
