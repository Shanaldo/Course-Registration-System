//Names: Shanaldo Carty
//ID#'s: 2108949
//Completed Date: Pending,2025

import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        CourseRegistrationSystem crs = new CourseRegistrationSystem();
        Scanner scanner = new Scanner(System.in);
        int menuChoice = -1;

        while (menuChoice != 0) {
            System.out.println("\n+-------------------------------------------+");
            System.out.println("| Welcome to the Course Registration System |");
            System.out.println("+-------------------------------------------+");
            System.out.println("| 1. Admin Menu                             |");
            System.out.println("| 2. Student Menu                           |");
            System.out.println("| 0. Exit                                   |");
            System.out.println("+-------------------------------------------+");
            System.out.print("Select a menu option: ");

            // Loop until a valid integer is entered
            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // Clear the invalid input
                System.out.print("Select a menu option: ");
            }
            menuChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (menuChoice) {
                case 1 -> handleLogin(crs, scanner, "admin");
                case 2 -> handleLogin(crs, scanner, "student");
                case 0 -> System.out.println("Exiting the system. Goodbye!");
                default -> System.out.println("Invalid option. Please choose 0, 1, or 2.");
            }
        }
        scanner.close();
    }

    // Handles the login process and menu redirection
    private static void handleLogin(CourseRegistrationSystem crs, Scanner scanner, String role) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (crs.authenticateUser(username, password, role)) {
            System.out.println("Login successful!");
            if (role.equals("admin")) {
                adminMenu(crs, scanner);
            } else {
                // Retrieve student ID by username for student role
                String studentId = crs.getStudentIdByUsername(username);
                if (studentId != null) {
                    studentMenu(crs, scanner, studentId); // Pass student ID to student menu
                } else {
                    System.out.println("Student ID not found. Please contact support.");
                }
            }
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }

    // Admin Menu with full access to system functionality
    public static void adminMenu(CourseRegistrationSystem crs, Scanner scanner) {
        int choice = -1;

        do {
            try {
                System.out.println("\n+----------------------------------+");
                System.out.println("|           Admin Menu             |");
                System.out.println("+----------------------------------+");
                System.out.println("| 1. Student Related Options       |");
                System.out.println("| 2. Course Related Options        |");
                System.out.println("| 3. Grade Related Options         |");
                System.out.println("| 0. Logout                        |");
                System.out.println("+----------------------------------+");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume ne

                switch (choice) {
                    case 1 -> studentAdminMenu(crs, scanner); // Call student submenu
                    case 2 -> courseAdminMenu(crs, scanner);  // Call course submenu
                    case 3 -> gradeAdminMenu(crs, scanner);   // Call grade submenu
                    case 0 -> System.out.println("Logging out...");
                    default -> System.out.println("Invalid choice. Please enter a valid number.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
            }
        } while (choice != 0);
    }


    // Student Menu with limited access
    public static void studentMenu(CourseRegistrationSystem crs, Scanner scanner, String studentId) {
        int choice = -1;

        do {
            try {
                System.out.println("\n+----------------------------------+");
                System.out.println("|           Student Menu           |");
                System.out.println("+----------------------------------+");
                System.out.println("| 1. Enroll in Course              |");
                System.out.println("| 2. View Enrolled Courses         |");
                System.out.println("| 3. Display All Courses           |");
                System.out.println("| 4. Undo Last Registration        |");
                System.out.println("| 5. Remove Course                 |");
                System.out.println("| 6. View Completed Courses        |");
                System.out.println("| 7. View Failed Courses           |");
                System.out.println("| 8. View Personal Details         |");
                System.out.println("| 9. View Course Grades            |");
                System.out.println("|10. View Waitlist Positions       |");
                System.out.println("|11. View Priority Queue Positions |");
                System.out.println("| 0. Logout                        |");
                System.out.println("+----------------------------------+");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> enrollSelfInCourse(crs, scanner, studentId);
                    case 2 -> viewEnrolledCourses(crs, studentId);
                    case 3 -> crs.displayAllCourses();
                    case 4 -> crs.undoLastRegistrationForStudent(studentId);
                    case 5 -> removeSelfFromCourse(crs, scanner, studentId);
                    case 6 -> viewCompletedCourses(crs, studentId);
                    case 7 -> crs.viewFailedCoursesForStudent(studentId);
                    case 8 -> viewPersonalDetails(crs, studentId);
                    case 9 -> crs.viewGradesForStudent(studentId);
                    case 10 -> crs.viewStudentWaitlistPositions(studentId);    // New case
                    case 11 -> crs.viewStudentPriorityQueuePositions(studentId); // New case
                    case 0 -> System.out.println("Logging out...");
                    default -> System.out.println("Invalid choice. Please enter a valid number.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
            }
        } while (choice != 0);
    }

    // Student Menu with limited access
    public static void studentAdminMenu(CourseRegistrationSystem crs, Scanner scanner) {
        int choice = -1;

        do {
            try {
                System.out.println("\n+----------------------------------+");
                System.out.println("|      Student Management          |");
                System.out.println("+----------------------------------+");
                System.out.println("| 1. Add Student                   |");
                System.out.println("| 2. Remove Student                |");
                System.out.println("| 3. Modify Student                |");
                System.out.println("| 4. Display All Students          |");
                System.out.println("| 5. View Student Details          |");
                System.out.println("| 0. Back to Admin Menu            |");
                System.out.println("+----------------------------------+");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> addStudent(crs, scanner);
                    case 2 -> removeStudent(crs, scanner);
                    case 3 -> modifyStudent(crs, scanner);
                    case 4 -> crs.displayAllStudents();
                    case 5 -> viewStudentDetails(crs, scanner);
                    case 0 -> System.out.println("Returning to Admin Menu...");
                    default -> System.out.println("Invalid choice. Please enter a valid number.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
            }
        } while (choice != 0);
    }

    public static void courseAdminMenu(CourseRegistrationSystem crs, Scanner scanner) {
        int choice = -1;
        do {
            try {
                System.out.println("\n+----------------------------------+");
                System.out.println("|       Course Management         |");
                System.out.println("+----------------------------------+");
                System.out.println("| 1. Add Course                   |");
                System.out.println("| 2. Remove Course                |");
                System.out.println("| 3. Modify Course                |");
                System.out.println("| 4. Enroll Student in Course     |");
                System.out.println("| 5. Undo Last Registration       |");
                System.out.println("| 6. Display All Courses          |");
                System.out.println("| 7. View Course Details          |");
                System.out.println("| 8. Remove Student from Course   |");
                System.out.println("| 9. Remove Student from Waitlist |");
                System.out.println("|10. Remove Student from Priority |");
                System.out.println("|11. View Waitlist by Course ID   |");
                System.out.println("|12. View Priority List by Course |");
                System.out.println("| 0. Back to Admin Menu           |");
                System.out.println("+----------------------------------+");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> addCourse(crs, scanner);
                    case 2 -> removeCourse(crs, scanner);
                    case 3 -> modifyCourse(crs, scanner);
                    case 4 -> enrollStudentInCourse(crs, scanner);
                    case 5 -> crs.undoLastRegistration();
                    case 6 -> crs.displayAllCourses();
                    case 7 -> viewCourseDetails(crs, scanner);
                    case 8 -> removeStudentFromCourse(crs, scanner);
                    case 9 -> removeStudentFromWaitlist(crs, scanner);
                    case 10 -> removeStudentFromPriorityQueue(crs, scanner);
                    case 11 -> viewWaitlistByCourseId(crs, scanner);
                    case 12 -> viewPriorityListByCourseId(crs, scanner);
                    case 0 -> System.out.println("Returning to Admin Menu...");
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
            }
        } while (choice != 0);
    }

    public static void gradeAdminMenu(CourseRegistrationSystem crs, Scanner scanner) {
        int choice = -1;
        do {
            try {
                System.out.println("\n+----------------------------------+");
                System.out.println("|          Grade Management        |");
                System.out.println("+----------------------------------+");
                System.out.println("| 1. View Grades for a Course      |");
                System.out.println("| 2. Enter Grades for a Course     |");
                System.out.println("| 3. Modify a Student's Grade      |");
                System.out.println("| 0. Back to Admin Menu            |");
                System.out.println("+----------------------------------+");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> viewGradesForCourse(crs, scanner);
                    case 2 -> enterGradesForCourse(crs, scanner);
                    case 3 -> modifyGradeForStudent(crs, scanner);
                    case 0 -> System.out.println("Returning to Admin Menu...");
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number between 0-3.");
                scanner.nextLine(); // Clear the invalid input
            }
        } while (choice != 0);
    }


    // Reuse my existing logic here by adding them as methods

    private static void addCourse(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter course ID (e.g., CMP1001): ");
        String courseId = scanner.nextLine().trim();
        if (!courseId.matches("[A-Z]{3}\\d{4}")) {
            System.out.println("Invalid course ID. It must contain exactly 3 uppercase letters followed by 4 digits.");
            return;
        }
        if (crs.findCourseById(courseId) != null) {
            System.out.println("A course with ID " + courseId + " already exists. Cannot add duplicate course ID.");
            return;
        }
        System.out.print("Enter course name: ");
        String courseName = scanner.nextLine().trim();
        if (!courseName.matches("[A-Z][a-zA-Z\\s]*")) {
            System.out.println("Invalid course name. It must start with a capital letter.");
            return;
        }

        // Validate course credits
        int credits = -1;
        while (true) {
            System.out.print("Enter course credits (0 or positive integer): ");
            if (scanner.hasNextInt()) {
                credits = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (credits >= 0) {  // Allow 0 or any positive integer
                    break;
                } else {
                    System.out.println("Error: Course credits must be zero or a positive integer.");
                }
            } else {
                System.out.println("Invalid input. Please enter a non-negative integer.");
                scanner.nextLine(); // Clear invalid input
            }
        }

        // Validate course capacity
        int capacity = 0;
        while (true) {
            System.out.print("Enter course capacity (positive integer): ");
            // Check if the next input is an integer
            if (scanner.hasNextInt()) {
                capacity = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                // Check if capacity is a positive integer
                if (capacity > 0) {
                    break; // Exit loop if the input is valid
                } else {
                    System.out.println("Error: Course capacity must be a positive integer greater than zero.");
                }
            } else {
                // Handle non-integer input
                System.out.println("Invalid input. Please enter a positive integer.");
                scanner.nextLine(); // Clear invalid input
            }
        }


        System.out.print("Enter prerequisites as a comma-separated list (e.g., CMP1001,CMP2001), or leave empty for none: ");
        String prerequisitesInput = scanner.nextLine().trim();
        LinkedList<String> prerequisites = new LinkedList<>();
        if (!prerequisitesInput.isEmpty()) {
            for (String prerequisite : prerequisitesInput.split(",")) {
                prerequisites.add(prerequisite.trim());
            }
        }

        Course course = new Course(courseId, courseName, credits, capacity, prerequisites);
        crs.addCourse(course);
        System.out.println("Course added successfully.");
    }

    private static void enrollStudentInCourse(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter student ID to enroll: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("Enter course ID: ");
        String courseId = scanner.nextLine().trim();
        crs.enrollStudentInCourse(studentId, courseId);
    }

    private static void removeCourse(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter course ID to remove: ");
        String courseId = scanner.nextLine().trim();
        crs.removeCourse(courseId);
    }

    private static void viewCourseDetails(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter course ID to view details: ");
        String courseId = scanner.nextLine().trim();
        crs.viewCourseDetails(courseId);
    }

    private static void removeStudentFromWaitlist(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter student ID to remove from waitlist: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("Enter course ID: ");
        String courseId = scanner.nextLine().trim();
        crs.removeStudentFromWaitlist(studentId, courseId);
    }

    private static void removeStudentFromPriorityQueue(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter student ID to remove from priority queue: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("Enter course ID: ");
        String courseId = scanner.nextLine().trim();
        crs.removeStudentFromPriorityQueue(studentId, courseId);
    }

    // New methods in Main to handle viewing the waitlist and priority queue
    private static void viewWaitlistByCourseId(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter course ID to view waitlist: ");
        String courseId = scanner.nextLine().trim();
        crs.viewWaitlistByCourseId(courseId);
    }

    private static void viewPriorityListByCourseId(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter course ID to view priority list: ");
        String courseId = scanner.nextLine().trim();
        crs.viewPriorityListByCourseId(courseId);
    }

    private static void modifyCourse(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter course ID to modify: ");
        String courseId = scanner.nextLine().trim();

        // Check if the course exists before proceeding
        if (crs.findCourseById(courseId) == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        System.out.print("Enter new course name: ");
        String newCourseName = scanner.nextLine().trim();
        if (!newCourseName.matches("[A-Z][a-zA-Z\\s]*")) {
            System.out.println("Invalid course name. It must start with a capital letter.");
            return;
        }

        // Validate new course credits
        int newCredits = -1;
        while (true) {
            System.out.print("Enter new course credits (0 or positive integer): ");
            if (scanner.hasNextInt()) {
                newCredits = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (newCredits >= 0) {  // Allow 0 or any positive integer
                    break;
                } else {
                    System.out.println("Error: Course credits must be zero or a positive integer.");
                }
            } else {
                System.out.println("Invalid input. Please enter a non-negative integer.");
                scanner.nextLine(); // Clear invalid input
            }
        }

        // Validate new course capacity
        int newCapacity = 0;
        while (true) {
            System.out.print("Enter new course capacity (positive integer): ");
            if (scanner.hasNextInt()) {
                newCapacity = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (newCapacity > 0) { // Ensure positive integer greater than zero
                    break;
                } else {
                    System.out.println("Error: Course capacity must be a positive integer greater than zero.");
                }
            } else {
                System.out.println("Invalid input. Please enter a positive integer.");
                scanner.nextLine(); // Clear invalid input
            }
        }
        scanner.nextLine(); // Consume the newline character after reading an integer

        // Prompt for prerequisites
        System.out.print("Enter new prerequisites as a comma-separated list (e.g., CMP1001,CMP2001), or leave empty for none: ");
        String newPrerequisitesInput = scanner.nextLine().trim();
        LinkedList<String> newPrerequisites = new LinkedList<>();
        if (!newPrerequisitesInput.isEmpty()) {
            for (String prerequisite : newPrerequisitesInput.split(",")) {
                newPrerequisites.add(prerequisite.trim());
            }
        }
        // Update the course with the new details
        crs.modifyCourse(courseId, newCourseName, newCredits, newCapacity, newPrerequisites);
        System.out.println("Course modified successfully.");
    }

    // Method to add a new student from the admin menu
    public static void addStudent(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter student ID (7 digits): ");
        String studentId = scanner.nextLine().trim();
        if (!studentId.matches("\\d{7}")) {
            System.out.println("Invalid student ID. It must be exactly 7 digits.");
            return;
        }

        // Check for duplicate student ID immediately
        if (crs.getStudentById(studentId) != null) {
            System.out.println("A student with ID " + studentId + " already exists. Cannot add duplicate student ID.");
            return;
        }

        System.out.print("Enter student name (e.g., John Doe): ");
        String studentName = scanner.nextLine().trim();
        if (!studentName.matches("[A-Z][a-zA-Z]+ [A-Z][a-zA-Z]+")) {
            System.out.println("Invalid student name. It must contain a first and last name, each starting with a capital letter.");
            return;
        }

        Student student = new Student(studentId, studentName);
        crs.addStudent(student); // Adds student with their ID as default password

        // Also add the user with default password as the student ID
        crs.addUser(studentName, studentId, "student");
        System.out.println("Student added successfully with ID# as default password.");
    }

    // New method for student self-enrollment
    public static void enrollSelfInCourse(CourseRegistrationSystem crs, Scanner scanner, String studentId) {
        System.out.println("\n+----------------------------------+");
        System.out.println("|       Course Enrollment          |");
        System.out.println("+----------------------------------+");

        System.out.print("Enter course ID to enroll in: ");
        String courseId = scanner.nextLine().trim();

        // Call the enrollSelfInCourse method in CourseRegistrationSystem
        crs.enrollSelfInCourse(studentId, courseId);
    }

    // Method to view enrolled courses
    public static void viewEnrolledCourses(CourseRegistrationSystem crs, String studentId) {
        System.out.println("\n+----------------------------------+");
        System.out.println("|       Enrolled Courses           |");
        System.out.println("+----------------------------------+");

        crs.viewEnrolledCourses(studentId);
    }

    // Method to view completed courses
    public static void viewCompletedCourses(CourseRegistrationSystem crs, String studentId) {
        System.out.println("\n+----------------------------------+");
        System.out.println("|       Completed Courses          |");
        System.out.println("+----------------------------------+");

        crs.viewCompletedCourses(studentId);
    }

    public static void viewFailedCoursesForStudent(CourseRegistrationSystem crs, String studentId) {
        System.out.println("\n+----------------------------------+");
        System.out.println("|       Failed Courses          |");
        System.out.println("+----------------------------------+");

        crs.viewFailedCoursesForStudent(studentId);
    }

    // Method to view personal details
    public static void viewPersonalDetails(CourseRegistrationSystem crs, String studentId) {
        System.out.println("\n+----------------------------------+");
        System.out.println("|        Personal Details          |");
        System.out.println("+----------------------------------+");

        crs.viewPersonalDetails(studentId);
    }

    public static void removeStudent(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter student ID to remove: ");
        String studentId = scanner.nextLine().trim();
        crs.removeStudent(studentId);
    }

    public static void modifyStudent(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter student ID to modify: ");
        String studentId = scanner.nextLine().trim();

        // Check if the student exists
        Student student = crs.getStudentById(studentId);
        if (student == null) {
            System.out.println("No student found with ID: " + studentId);
            return;
        }

        // Prompt for new name only if the student exists
        System.out.print("Enter new student name: ");
        String newName = scanner.nextLine().trim();
        if (!newName.matches("[A-Z][a-zA-Z]+ [A-Z][a-zA-Z]+")) {
            System.out.println("Invalid student name. It must contain a first and last name, each starting with a capital letter.");
            return;
        }

        // Proceed with modification if the student exists and name is valid
        crs.modifyStudent(studentId, newName);
        System.out.println("Student with ID " + studentId + " has been updated successfully.");
    }


    public static void viewStudentDetails(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter student ID to view details: ");
        String studentId = scanner.nextLine().trim();
        crs.viewStudentDetails(studentId);
    }


    // View grades for a course
    public static void viewGradesForCourse(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.println("\n+----------------------------------+");
        System.out.println("|       View Grades by Course      |");
        System.out.println("+----------------------------------+");

        System.out.print("Enter course ID to view grades: ");
        String courseId = scanner.nextLine();
        crs.viewGradesForCourse(courseId);
    }

    // Enter grades for a course
    public static void enterGradesForCourse(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter course ID to enter grades: ");
        String courseId = scanner.nextLine();
        crs.enterGradesForCourse(courseId, scanner);
    }

    // Modify a student's grade for a course
    public static void modifyGradeForStudent(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.println("\n+----------------------------------+");
        System.out.println("|      Modify Student's Grade      |");
        System.out.println("+----------------------------------+");

        System.out.print("Enter student ID to modify grade: ");
        String studentId = scanner.nextLine();
        System.out.print("Enter course ID: ");
        String courseId = scanner.nextLine();
        System.out.print("Enter new grade (A+, A, A-, B+, B, B-, C+, C, F): ");
        String newGrade = scanner.nextLine().toUpperCase();
        crs.modifyGradeForStudent(studentId, courseId, newGrade);
    }

    private static void removeStudentFromCourse(CourseRegistrationSystem crs, Scanner scanner) {
        System.out.print("Enter student ID: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("Enter course ID: ");
        String courseId = scanner.nextLine().trim();
        crs.removeCourseFromStudent(studentId, courseId, true); // Pass true for admin
    }

    private static void removeSelfFromCourse(CourseRegistrationSystem crs, Scanner scanner, String studentId) {
        System.out.print("Enter course ID to remove: ");
        String courseId = scanner.nextLine().trim();
        crs.removeCourseFromStudent(studentId, courseId, false); // Pass false for student
    }
}