import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AdminService } from '../../../services/admin.service';
import { ReportsResponse } from '../../../models/admin.model';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, ChartData, ChartType, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent {
  reports?: ReportsResponse;

  // Chart: Decision Outcomes (Bar)
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    scales: {
      x: {},
      y: { min: 0 }
    },
    plugins: {
      legend: { display: false }
    }
  };
  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: ['Approved', 'Rejected'],
    datasets: [
      { data: [0, 0], backgroundColor: ['#10B981', '#EF4444'], borderRadius: 4 }
    ]
  };

  // Chart: Approval Rate (Doughnut)
  public doughnutChartLabels: string[] = ['Approved', 'Rejected'];
  public doughnutChartData: ChartData<'doughnut'> = {
    labels: this.doughnutChartLabels,
    datasets: [{ data: [0, 0], backgroundColor: ['#10B981', '#F3F4F6'] }]
  };
  public doughnutChartType: ChartType = 'doughnut';
  public doughnutChartOptions: any = {
    responsive: true,
    cutout: '80%',
    plugins: {
      legend: { display: false }
    }
  };

  constructor(private readonly adminService: AdminService) {
    this.adminService.getReports().subscribe((res) => {
      this.reports = res;
      if (this.reports) {
        // Update Bar Chart
        this.barChartData.datasets[0].data = [this.reports.approved, this.reports.rejected];
        
        // Update Doughnut Chart
        const rate = parseFloat(this.reports.approvalRate.replace('%', ''));
        this.doughnutChartData.datasets[0].data = [rate, 100 - rate];
      }
    });
  }
}
