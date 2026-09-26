export const APP_NAME = 'Bluejay';

export const dashboardMetrics = [
  { label: 'Today Revenue', value: '$4,280.50' },
  { label: 'Transactions', value: '143' },
  { label: 'Active Cashiers', value: '5' },
  { label: 'Low Stock Items', value: '8' }
];

export const products = [
  { code: 'PRD-001', barcode: '1001001', name: 'Premium Rice 5kg', category: 'Grocery', price: 11.9 },
  { code: 'PRD-002', barcode: '1001002', name: 'Cooking Oil 2L', category: 'Grocery', price: 8.2 },
  { code: 'PRD-003', barcode: '1001003', name: 'Laundry Soap', category: 'Household', price: 2.1 },
  { code: 'PRD-004', barcode: '1001004', name: 'Brown Sugar 1kg', category: 'Grocery', price: 1.85 },
  { code: 'PRD-005', barcode: '1001005', name: 'Instant Noodles Pack', category: 'Grocery', price: 3.4 },
  { code: 'PRD-006', barcode: '1001006', name: 'Milk 1L', category: 'Dairy', price: 2.65 },
  { code: 'PRD-007', barcode: '1001007', name: 'Eggs 12pcs', category: 'Dairy', price: 3.95 },
  { code: 'PRD-008', barcode: '1001008', name: 'Orange Juice 1L', category: 'Beverage', price: 2.8 },
  { code: 'PRD-009', barcode: '1001009', name: 'Dishwashing Liquid', category: 'Household', price: 4.35 },
  { code: 'PRD-010', barcode: '1001010', name: 'Bath Tissue 6 Rolls', category: 'Household', price: 5.6 }
];

export const stockHealth = [
  { item: 'Premium Rice 5kg', level: 72, status: 'Healthy', statusClass: 'status-ok' },
  { item: 'Cooking Oil 2L', level: 34, status: 'Low', statusClass: 'status-low' },
  { item: 'Laundry Soap', level: 18, status: 'Critical', statusClass: 'status-critical' }
];

export const reportItems = [
  'Daily sales summary',
  'Inventory valuation',
  'Low stock reorder list'
];

export const demoUsers = [
  {
    username: 'admin',
    password: 'admin123',
    displayName: 'Ava Admin',
    role: 'Admin',
    homePage: 'dashboard',
    allowedPages: ['dashboard', 'products', 'inventory', 'sales', 'reports', 'users']
  },
  {
    username: 'stock.manager',
    password: 'stock123',
    displayName: 'Milo Stock Manager',
    role: 'Stock Manager',
    homePage: 'inventory',
    allowedPages: ['dashboard', 'products', 'inventory']
  },
  {
    username: 'cashier',
    password: 'cashier123',
    displayName: 'Casey Cashier',
    role: 'Cashier',
    homePage: 'sales',
    allowedPages: ['sales']
  }
];
