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
    this.usersService.registerUser(this.user).subscribe(data => {
      console.log(data);
    },
    error => console.log(error));
  }

  onSubmit() {
    console.log(this.user);
    this.saveUser();
  }

}
