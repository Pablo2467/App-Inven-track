import { CrudConfig } from './crud.models';

export const PRODUCTS: CrudConfig = {
  title: 'Productos',
  path: '/products',
  newLabel: 'Nuevo producto',
  canCreate: true,
  canEdit: true,
  canDelete: true,
  fields: [
    { key: 'code', label: 'Código', required: true },
    { key: 'name', label: 'Nombre', required: true },
    { key: 'description', label: 'Descripción', type: 'textarea' },
    { key: 'category', label: 'Categoría' },
    { key: 'unit', label: 'Unidad (kg, paquete...)', required: true },
    { key: 'minStock', label: 'Stock mínimo', type: 'number', required: true },
  ],
  columns: [
    { key: 'code', label: 'Código' },
    { key: 'name', label: 'Nombre' },
    { key: 'category', label: 'Categoría' },
    { key: 'unit', label: 'Unidad' },
    { key: 'minStock', label: 'Stock mínimo' },
    { key: 'status', label: 'Estado' },
  ],
};

export const SUPPLIERS: CrudConfig = {
  title: 'Proveedores',
  path: '/suppliers',
  newLabel: 'Nuevo proveedor',
  canCreate: true,
  canEdit: true,
  canDelete: true,
  fields: [
    { key: 'name', label: 'Nombre o razón social', required: true },
    { key: 'taxId', label: 'NIT', required: true },
    { key: 'phone', label: 'Teléfono' },
    { key: 'email', label: 'Correo' },
    { key: 'address', label: 'Dirección' },
  ],
  columns: [
    { key: 'name', label: 'Nombre' },
    { key: 'taxId', label: 'NIT' },
    { key: 'phone', label: 'Teléfono' },
    { key: 'email', label: 'Correo' },
    { key: 'status', label: 'Estado' },
  ],
};

export const ENTRIES: CrudConfig = {
  title: 'Entradas de inventario',
  path: '/stock-entries',
  newLabel: 'Registrar entrada',
  canCreate: true,
  canEdit: false,
  canDelete: false,
  fields: [
    { key: 'productId', label: 'Producto', type: 'select', required: true, optionsUrl: '/products' },
    { key: 'supplierId', label: 'Proveedor', type: 'select', required: true, optionsUrl: '/suppliers' },
    { key: 'quantity', label: 'Cantidad', type: 'number', required: true },
    { key: 'lotNumber', label: 'Número de lote (opcional)' },
    { key: 'expirationDate', label: 'Fecha de vencimiento (opcional)', type: 'date' },
    { key: 'unitCost', label: 'Costo unitario (opcional)', type: 'number' },
    { key: 'notes', label: 'Notas', type: 'textarea' },
  ],
  columns: [
    { key: 'lotNumber', label: 'Lote' },
    { key: 'productName', label: 'Producto' },
    { key: 'supplierName', label: 'Proveedor' },
    { key: 'quantity', label: 'Cantidad' },
    { key: 'entryDate', label: 'Ingreso' },
    { key: 'expirationDate', label: 'Vence' },
    { key: 'userName', label: 'Registró' },
  ],
};

export const EXITS: CrudConfig = {
  title: 'Salidas de inventario (FIFO)',
  path: '/stock-exits',
  newLabel: 'Registrar salida',
  canCreate: true,
  canEdit: false,
  canDelete: false,
  fields: [
    { key: 'productId', label: 'Producto', type: 'select', required: true, optionsUrl: '/products' },
    { key: 'quantity', label: 'Cantidad', type: 'number', required: true },
    {
      key: 'reason',
      label: 'Motivo',
      type: 'select',
      required: true,
      options: [
        { value: 'SALE', label: 'Venta' },
        { value: 'EXPIRED_DISPOSAL', label: 'Baja por vencimiento' },
        { value: 'DAMAGE', label: 'Daño' },
        { value: 'INTERNAL_USE', label: 'Uso interno' },
        { value: 'OTHER', label: 'Otro' },
      ],
    },
    { key: 'notes', label: 'Notas', type: 'textarea' },
  ],
  columns: [
    { key: 'productName', label: 'Producto' },
    { key: 'quantity', label: 'Cantidad' },
    { key: 'reason', label: 'Motivo' },
    { key: 'exitDate', label: 'Fecha' },
    { key: 'userName', label: 'Registró' },
  ],
};

export const BATCHES: CrudConfig = {
  title: 'Lotes (se descuentan primero los más antiguos)',
  path: '/batches',
  newLabel: '',
  canCreate: false,
  canEdit: false,
  canDelete: false,
  fields: [],
  columns: [
    { key: 'lotNumber', label: 'Lote' },
    { key: 'productName', label: 'Producto' },
    { key: 'initialQuantity', label: 'Inicial' },
    { key: 'availableQuantity', label: 'Disponible' },
    { key: 'entryDate', label: 'Ingreso' },
    { key: 'expirationDate', label: 'Vence' },
    { key: 'status', label: 'Estado' },
  ],
};