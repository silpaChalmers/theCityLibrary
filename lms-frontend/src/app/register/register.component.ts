import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Users } from '../_model/users';
import { UsersService } from '../_service/users.service';

@Component({
  selector: 'app-registration',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  user: Users = new Users();
  constructor(private usersService: UsersService,
    private router: Router) { }

  ngOnInit(): void {
     this.user.role = [{ roleName: 'User' }];
  }

  saveUser() {
      this.usersService.registerUser(this.user).subscribe({
        next: (data) => {
          console.log(data);
          alert('Registration successful! Please log in using your new account.');
          this.router.navigate(['/login']);
        },
        error: (err) => {
          console.error(err);
          alert('Registration failed. Please try again later.');
        }
      });
    }

  onSubmit() {
    console.log(this.user);
    this.saveUser();
  }

}
