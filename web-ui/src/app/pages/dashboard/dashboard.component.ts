import { CurrencyPipe } from '@angular/common';
import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-dashboard',
  imports: [MatCardModule, MatChipsModule, CurrencyPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  readonly metrics = [
    { label: 'Today Revenue', value: 4280.5 },
    { label: 'Transactions', value: 143 },
    { label: 'Active Cashiers', value: 5 },
    { label: 'Low Stock Items', value: 8 }
  ];
}
