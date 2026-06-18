import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Employee, EmployeeSearchCriteria, FilterOptions } from '../models/employee.model';

/**
 * Employee API client (DESIGN §6.4). Single place the SPA talks to the employee/meta endpoints.
 */
@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly base = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  search(criteria: EmployeeSearchCriteria): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.base}/employees`, {
      params: this.toParams(criteria)
    });
  }

  getFilterOptions(): Observable<FilterOptions> {
    return this.http.get<FilterOptions>(`${this.base}/meta/filters`);
  }

  /** Export current result set; resolves the filename from Content-Disposition (DESIGN §6.4). */
  export(criteria: EmployeeSearchCriteria): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.base}/employees/export`, {
      params: this.toParams(criteria),
      responseType: 'blob',
      observe: 'response'
    });
  }

  /** Only send filters that are active (non-empty and not "All"), mirroring BR-4. */
  private toParams(criteria: EmployeeSearchCriteria): HttpParams {
    let params = new HttpParams();
    if (criteria.name && criteria.name.trim()) {
      params = params.set('name', criteria.name.trim());
    }
    if (criteria.department && criteria.department !== 'All') {
      params = params.set('department', criteria.department);
    }
    if (criteria.role && criteria.role !== 'All') {
      params = params.set('role', criteria.role);
    }
    if (criteria.status && criteria.status !== 'All') {
      params = params.set('status', criteria.status);
    }
    return params;
  }
}
