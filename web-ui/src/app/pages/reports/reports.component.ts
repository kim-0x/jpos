import { DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';

@Component({
  selector: 'app-reports',
  imports: [MatCardModule, MatButtonModule, MatListModule, DatePipe],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent {
  readonly generatedAt = new Date();

  readonly reportItems = [
    'Daily sales summary',
    'Inventory valuation',
    'Low stock reorder list'
  ];
}
