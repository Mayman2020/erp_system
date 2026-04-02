import {Directive, ElementRef, HostListener} from '@angular/core';

@Directive({ standalone: false,
  selector: '[appTodoCardComplete]'
})
export class TodoCardCompleteDirective {
  constructor(private elements: ElementRef) {}

  @HostListener('click', ['$event'])
  onToggle($event: any) {
    $event.preventDefault();
    this.elements.nativeElement.classList.toggle('complete');
  }
}
