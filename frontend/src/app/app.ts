import { Component } from '@angular/core';
import {
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';

import { Toastr } from './components/toastr/toastr';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    Toastr
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
}