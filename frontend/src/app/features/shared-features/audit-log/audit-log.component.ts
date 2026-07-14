import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'app-audit-log',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './audit-log.component.html',
    styleUrl: './audit-log.component.css'
})
export class AuditLogComponent implements OnInit {
    logs: any[] = [];
    filteredLogs: any[] = [];
    loading = true;
    errorMessage = '';

    searchTerm = '';
    selectedType = '';

    constructor(private http: HttpClient) {}

    ngOnInit(): void {
        this.fetchLogs();
    }

    fetchLogs(): void {
        this.loading = true;
        this.errorMessage = '';

        this.http.get<any[]>('http://localhost:9098/api/v1/registrar/audit-logs').subscribe({
            next: (data) => {
                this.logs = data || [];
                this.filteredLogs = [...this.logs];
                this.loading = false;
            },
            error: (err) => {
                this.errorMessage = err?.error?.message || 'Failed to sync system database audit logs.';
                this.loading = false;
            }
        });
    }

    applyFilters(): void {
        this.filteredLogs = this.logs.filter(log => {
            const matchesSearch = !this.searchTerm ||
                (log.doneBy && log.doneBy.toLowerCase().includes(this.searchTerm.toLowerCase())) ||
                (log.eventName && log.eventName.toLowerCase().includes(this.searchTerm.toLowerCase())) ||
                (log.eventMessage && log.eventMessage.toLowerCase().includes(this.searchTerm.toLowerCase()));

            const matchesType = !this.selectedType || log.eventType === this.selectedType;

            return matchesSearch && matchesType;
        });
    }

    getBadgeClass(type: string): string {
        switch (type?.toUpperCase()) {
            case 'CREATE': return 'bg-success text-white';
            case 'UPDATE': return 'bg-warning text-dark';
            case 'DELETE': return 'bg-danger text-white';
            case 'AUTH': return 'bg-info text-dark';
            default: return 'bg-secondary text-white';
        }
    }
}