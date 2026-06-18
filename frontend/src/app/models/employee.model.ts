export interface Employee {
  id: number;
  name: string;
  department: string;
  role: string;
  status: string;
  email: string;
  phone: string;
  hireDate: string;
  salary: number;
}

export interface EmployeeSearchCriteria {
  name?: string;
  department?: string;
  role?: string;
  status?: string;
}

export interface FilterOptions {
  departments: string[];
  roles: string[];
  statuses: string[];
}
