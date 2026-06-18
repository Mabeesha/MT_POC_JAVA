import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';
import { EmployeeService } from '../services/employee.service';
import { Employee, EmployeeSearchCriteria, FilterOptions } from '../models/employee.model';

/** Status cell colors (BR-5). */
const STATUS_COLORS: Record<string, { bg: string; fg: string }> = {
  Active: { bg: '#D1FAE5', fg: '#065F46' },
  Inactive: { bg: '#FEE2E2', fg: '#991B1B' },
  'On Leave': { bg: '#FEF3C7', fg: '#92400E' }
};
const STATUS_DEFAULT = { bg: '#E5E7EB', fg: '#374151' };

/**
 * Search screen (UI-2, FR-3..FR-6). Header, filters (from M1), results grid with status colors
 * and currency-formatted salary, and a status bar mirroring the desktop messages.
 */
@Component({
  selector: 'app-search',
  imports: [
    CommonModule,
    FormsModule,
    MatToolbarModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    MatSnackBarModule
  ],
  templateUrl: './search.component.html',
  styleUrl: './search.component.css'
})
export class SearchComponent implements OnInit {
  readonly displayedColumns = [
    'id', 'name', 'department', 'role', 'status', 'email', 'phone', 'hireDate', 'salary'
  ];

  name = '';
  department = 'All';
  role = 'All';
  status = 'All';

  readonly filterOptions = signal<FilterOptions>({ departments: ['All'], roles: ['All'], statuses: ['All'] });
  readonly results = signal<Employee[]>([]);
  readonly statusBar = signal('Use filters above and click Search to find employees.');

  constructor(
    private employees: EmployeeService,
    private auth: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  get username(): string | null {
    return this.auth.currentUsername();
  }

  ngOnInit(): void {
    this.employees.getFilterOptions().subscribe({
      next: (opts) => this.filterOptions.set(opts)
    });
    // FR-3 trigger (a): auto-execute with no filters on load.
    this.search();
  }

  private criteria(): EmployeeSearchCriteria {
    return { name: this.name, department: this.department, role: this.role, status: this.status };
  }

  search(): void {
    this.employees.search(this.criteria()).subscribe({
      next: (rows) => {
        this.results.set(rows);
        this.statusBar.set(this.countMessage(rows.length));
      },
      error: () => this.statusBar.set('Search failed. Please try again.')
    });
  }

  /** FR-4 — Clear filters and re-run. */
  clear(): void {
    this.name = '';
    this.department = 'All';
    this.role = 'All';
    this.status = 'All';
    this.search();
  }

  /** FR-6 — Logout: discard token and return to login. */
  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  /** FR-5 — Export. Client guard (BR-7) avoids a needless request when empty. */
  exportCsv(): void {
    if (this.results().length === 0) {
      this.snackBar.open('There are no rows to export.', 'Dismiss', { duration: 4000 });
      return;
    }
    this.employees.export(this.criteria()).subscribe({
      next: (res) => this.triggerDownload(res),
      error: (err: HttpErrorResponse) => {
        const detail = (err.error && (err.error as any).detail) || 'Export failed.';
        this.snackBar.open(detail, 'Dismiss', { duration: 4000 });
      }
    });
  }

  private triggerDownload(res: HttpResponse<Blob>): void {
    const blob = res.body as Blob;
    const filename = this.filenameFromDisposition(res.headers.get('Content-Disposition'))
      ?? 'employees.csv';
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);

    const n = this.results().length;
    this.statusBar.set(`Exported ${n} employee${n === 1 ? '' : 's'} to ${filename}.`);
  }

  private filenameFromDisposition(header: string | null): string | null {
    if (!header) {
      return null;
    }
    const match = /filename="?([^"]+)"?/.exec(header);
    return match ? match[1] : null;
  }

  private countMessage(count: number): string {
    if (count === 0) {
      return 'No employees match the selected filters.';
    }
    if (count === 1) {
      return '1 employee found.';
    }
    return `${count} employees found.`;
  }

  statusStyle(value: string): { [k: string]: string } {
    const c = STATUS_COLORS[value] ?? STATUS_DEFAULT;
    return { 'background-color': c.bg, color: c.fg };
  }
}
