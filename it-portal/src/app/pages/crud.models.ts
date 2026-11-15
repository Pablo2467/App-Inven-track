export interface Field {
  key: string;
  label: string;
  type?: 'text' | 'number' | 'date' | 'select' | 'textarea';
  required?: boolean;
  options?: { value: string; label: string }[];
  optionsUrl?: string;
  optionLabel?: string;
}

export interface Column {
  key: string;
  label: string;
}

export interface CrudConfig {
  title: string;
  path: string;
  newLabel: string;
  fields: Field[];
  columns: Column[];
  canCreate: boolean;
  canEdit: boolean;
  canDelete: boolean;
}