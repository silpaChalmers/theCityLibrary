import { Books } from './books'

export class Borrow {
    borrowId: number;
    bookId: number;
    userId: number;
    issueDate: Date;
    returnDate: Date;
    dueDate: Date;
    book?: Books;
}
