import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';

interface ProductRow {
  code: string;
  name: string;
  category: string;
  price: string;
}

@Component({
  selector: 'app-products',
  imports: [MatCardModule, MatTableModule, MatButtonModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss'
})
export class ProductsComponent {
  readonly displayedColumns = ['code', 'name', 'category', 'price'];
  readonly rows: ProductRow[] = [
    { code: 'PRD-001', name: 'Premium Rice 5kg', category: 'Grocery', price: '$11.90' },
    { code: 'PRD-002', name: 'Cooking Oil 2L', category: 'Grocery', price: '$8.20' },
    { code: 'PRD-003', name: 'Laundry Soap', category: 'Household', price: '$2.10' }
  ];
}
