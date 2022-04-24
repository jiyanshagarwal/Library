package library;

import database.StorageHashTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Jiyansh
 */
public class Library {

    final private StorageHashTable booksTable;
    final private StorageHashTable studentsTable;

    final private int maxBooks;
    final private int maxStudents;

    private ArrayList<String> books = new ArrayList<>();
    private ArrayList<String> students = new ArrayList<>();

    public Library(String bookFileName, String studentFileName, int maxNumOfBooks, int maxNumOfStudents) {
        booksTable = new StorageHashTable(System.getProperty("user.dir"), bookFileName, maxNumOfBooks, 100, 5, new int[]{100, 5, 10, 1, 100});
        studentsTable = new StorageHashTable(System.getProperty("user.dir"), studentFileName, maxNumOfStudents, 100, 5, new int[]{1, 100, 100, 100, 3});

        maxBooks = maxNumOfBooks;
        maxStudents = maxNumOfStudents;
        
        for (String book : booksTable.getKeys()) {
            if (book != null && book.length() > 0) {
                books.add(book);
            }
        }
        for (String student : studentsTable.getKeys()) {
            if (student != null && student.length() > 0) {
                students.add(student);
            }
        }
    }

    /**
     * Adds book to the library.
     *
     * @param title the book title. Must be under 100 characters.
     * @param author the author. Must be under 100 characters.
     * @param pageCount the number of pages in the book. Must be under 5 characters (i.e. under 100,000 pages).
     * @param language the language the book is written in. Must be under 10 characters.
     * @return true if successfully added.
     * @throws IOException if book title or data is longer than acceptable length.
     */
    public boolean addBook(String title, String author, String pageCount, String language) throws IOException {
        if (books.size() == maxBooks) {
            return false;
        }

        if (title.length() == 0 || author.length() == 0 || pageCount.length() == 0 || language.length() == 0) {
            throw new IOException("All fields must be filled!");
        } else if (title.length() > 100 || author.length() > 100 || pageCount.length() > 5 || language.length() > 10) {
            throw new IOException("Field has too many characters!");
        } else if (booksTable.containsKey(title)) {
            throw new IOException(title + " is already in the system!");
        } else if (booksTable.addEntry(title, new String[]{author, pageCount, language, "1", ""}) != -1) {
            books.add(title);
            return true;
        }
        return false;
    }

    /**
     * Removes book from the library.
     *
     * @param title The title of the book to remove. Must be under 100 characters.
     * @return true if successfully removed.
     * @throws IOException if book title is longer than acceptable length.
     */
    public boolean removeBook(String title) throws IOException {
        if (title == null || title.length() == 0) {
            throw new IOException("Title cannot be empty");
        } else if (title.length() > 100) {
            throw new IOException(title + " has too many characters!");
        } else if (!booksTable.containsKey(title)) {
            throw new IOException(title + " isn't in the system");
        } else if (Integer.parseInt(booksTable.readEntry(title)[3]) == 0) {
            throw new IOException(title + " is checked out. Check it in first!");
        } else if (booksTable.deleteEntry(title) != null) {
            books.remove(title);
            return true;
        }
        return false;
    }

    /**
     * Adds student to the library
     *
     * @param name the name of the student to add. Must be under 100 characters.
     * @return true if successfully added.
     * @throws IOException if student name is longer than acceptable length.
     */
    public boolean addStudent(String name) throws IOException {
        if (students.size() == maxStudents) {
            return false;
        }

        if (name == null || name.length() == 0) {
            throw new IOException("Name cannot be empty");
        } else if (name.length() > 100) {
            throw new IOException(name + " has too many characters!");
        } else if (studentsTable.containsKey(name)) {
            throw new IOException(name + " is already in the system.");
        } else if (studentsTable.addEntry(name, new String[]{"0", "", "", "", "0"}) != -1) {
            students.add(name);
            return true;
        }
        return false;
    }

    /**
     * Removes student from the library
     *
     * @param name the name of the student to remove. Must be under 100 characters.
     * @return true if successfully removed.
     * @throws IOException if student name is longer than acceptable length.
     */
    public boolean removeStudent(String name) throws IOException {
        if (name == null || name.length() == 0) {
            throw new IOException("Name cannot be empty");
        } else if (name.length() > 100) {
            throw new IOException(name + " has too many characters!");
        } else if (!studentsTable.containsKey(name)) {
            throw new IOException(name + " isn't in the system.");
        } else if (Integer.parseInt(studentsTable.readEntry(name)[0]) > 0) {
            throw new IOException(name + " has books checked out. Check those in before removing student.");
        } else if (studentsTable.deleteEntry(name) != null) {
            students.remove(name);
            return true;
        }
        return false;
    }

    /**
     * Checks a book out of the library. Sets it as unavailable and adds to <code>student</code>'s list of checked out books.
     *
     * @param book The book to checkout. Title cannot be longer than 100 characters.
     * @param student The student to checkout to. Name cannot be longer than 100 characters.
     * @return true if checked out.
     * @throws IOException if data length is invalid.
     */
    public boolean checkOutBook(String book, String student) throws IOException {
        if (book == null || student == null || book.length() == 0 || student.length() == 0) {
            throw new IOException("Student and/or book not selected!");
        } else if (!studentsTable.containsKey(student)) {
            throw new IOException(student + "not in the system!");
        } else if (!booksTable.containsKey(book)) {
            throw new IOException(book + " not in the system!");
        } else if (Integer.parseInt(booksTable.readEntry(book)[3]) == 0) {
            throw new IOException(book + " has already been checked out.");
        } else if (Integer.parseInt(studentsTable.readEntry(student)[0]) >= 3) {
            throw new IOException(student + " has already checked out 3 books. No more are allowed.");
        } else if (book.equals(studentsTable.readEntry(student)[1]) || book.equals(studentsTable.readEntry(student)[2])
                || book.equals(studentsTable.readEntry(student)[3])) {
            throw new IOException(student + " has already checked out this book.");
        } else {

            String[] studentData = studentsTable.readEntry(student);
            //Adds 1 to the count of books checked out by this student.
            studentData[0] = Integer.toString(Integer.parseInt(studentData[0]) + 1);

            //Finds empty spot to add book to list of books checked out by student.
            for (int i = 1; i < 4; i++) {
                if (studentData[i].length() == 0) {
                    studentData[i] = book;
                    break;
                }
            }

            //Adds 1 to the list of total books read by student.
            studentData[4] = Integer.toString(Integer.parseInt(studentData[4]) + 1);

            //Sets books as unavailable.
            String[] bookData = booksTable.readEntry(book);
            bookData[3] = "0";
            bookData[4] = student;

            studentsTable.changeRecords(student, studentData);
            booksTable.changeRecords(book, bookData);

            return true;
        }
    }

    /**
     * Checks in books by a student.
     *
     * @param books the books to check in. Must match the books the student has checked out.
     * @param student the name of the student whose books are being checked in. Must be under 100 characters.
     * @return true if successfully checked in.
     * @throws IOException if data length is incorrect or if books do not match the student.
     */
    public boolean checkInBook(String[] books, String student) throws IOException {

        if (student == null || student.length() == 0) {
            throw new IOException("Student not selected");
        } else if (!studentsTable.containsKey(student)) {
            throw new IOException(student + " not in the system!");
        } else if (Integer.parseInt(studentsTable.readEntry(student)[0]) == 0) {
            throw new IOException(student + " has no books.");
        }

        for (String book : books) {
            if (book == null || book.length() == 0) {
                throw new IOException("Book not selected!");
            } else if (!booksTable.containsKey(book)) {
                throw new IOException(book + " not in the system!");
            } else if (!booksTable.readEntry(book)[4].equals(student)) {
                throw new IOException(book + " is checked out to different student.");
            }
        }

        String[] studentData = studentsTable.readEntry(student);

        for (int i = 1; i < 4; i++) {
            for (String book : books) {
                if (studentData[i].equals(book)) {
                    studentData[i] = "";
                    studentData[0] = Integer.toString(Integer.parseInt(studentData[0]) - 1);
                }
            }
        }

        for (String book : books) {
            String[] bookData = booksTable.readEntry(book);
            bookData[3] = "1";
            bookData[4] = "";
            booksTable.changeRecords(book, bookData);
        }

        studentsTable.changeRecords(student, studentData);
        return true;
    }

    public String[] getBooks() {
        return books.toArray(new String[books.size()]);
    }

    public String[] getStudents() {
        return students.toArray(new String[students.size()]);
    }
    
    public String[] getBookInfo(String book) {        
        if (book == null || book.length() == 0) {
            return null;
        } else if (!booksTable.containsKey(book)) {
            return null;
        }
        
        return booksTable.readEntry(book);
    }
    
    public String[] getStudentInfo(String student) {
        if (student == null || student.length() == 0) {
            return null;
        } else if (!studentsTable.containsKey(student)) {
            return null;
        }
        
        return studentsTable.readEntry(student);
    }

    /**
     * Gets a list of books currently checked out by a student.
     *
     * @param student The name of the student.
     * @return The list of checked out books. Max length is 3.
     */
    public String[] getStudentBooks(String student) {
        if (student == null || student.length() == 0) {
            return null;
        } else if (!studentsTable.containsKey(student)) {
            return null;
        } else if (Integer.parseInt(studentsTable.readEntry(student)[0]) == 0) {
            return null;
        }
        
        ArrayList<String> studentBooks = new ArrayList<>();
        
        for (String book : Arrays.copyOfRange(studentsTable.readEntry(student), 1, 4)) {
            if (book.length() != 0) {
                studentBooks.add(book);
            }
        }

        return studentBooks.toArray(new String[studentBooks.size()]);
    }

    /**
     * Gets the current number of books checked out by the student.
     *
     * @param student The name of the student.
     * @return the number of books. -1 if error.
     */
    public int numOfBooksCheckedOutByStudent(String student) {
        if (student == null || student.length() == 0) {
            return -1;
        } else if (!studentsTable.containsKey(student)) {
            return -1;
        }

        return Integer.parseInt(studentsTable.readEntry(student)[0]);
    }

    public boolean bookIsCheckedOut(String book) {
        if (book == null || book.length() == 0) {
            return false;
        } else if (!booksTable.containsKey(book)) {
            return false;
        }

        return Integer.parseInt(booksTable.readEntry(book)[3]) == 0;
    }
}
