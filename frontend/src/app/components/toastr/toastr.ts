import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';

import { ToastrService } from '../../services/toastr.service';

@Component({
  selector: 'app-toastr',
  imports: [CommonModule],
  templateUrl: './toastr.html',
  styleUrl: './toastr.scss'
})
export class Toastr {
  readonly toastr = inject(ToastrService);
}
