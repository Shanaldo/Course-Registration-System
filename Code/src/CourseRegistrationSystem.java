//Names: Shanaldo Carty
//ID#'s: 2108949
//Completed Date: Pending,2025

import java.io.*;
import java.util.*;


public class CourseRegistrationSystem {
    private LinkedList<Student> students; // Linked list to store students
    private LinkedList<Course> courses; // Linked list to store courses
    private Stack<String> registrationHistory; // Stack to track last actions for undo
    private LinkedList<User> users; // List of User objects

    // File paths for data storage
    private final String studentFilePath = "students.dat";
    private final String courseFilePath = "courses.dat";
    private final String userFilePath = "users.dat";
    private final String registrationHistoryFilePath = "registrationHistory.dat";
    private final String gradesFilePath = "grades.csv"; // File path for grades


    public CourseRegistrationSystem() {
        this.students = new LinkedList<>();
        this.courses = new LinkedList<>();
        this.users = new LinkedList<>();
        this.registrationHistory = new Stack<>();

        loadUsersFromFile(); // Load users on startup
        loadStudentsFromFile();
        loadCoursesFromFile();
        loadRegistrationHistory(); // Load registration history on startup

        if (users.isEmpty()) {
            createDefaultUsers();
            saveUsersToFile(); // Save them so they persist
        }
    }

    public void saveRegistrationHistory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(registrationHistoryFilePath))) {
            oos.writeObject(registrationHistory);
            System.out.println("Registration history saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add a new student
    public void addStudent(Student student) {
        students.add(student);
        addUser(student.getName(), student.getId(), "student"); // Name as username, ID as password
        saveStudentsToFile();
        System.out.println("Student added successfully with name as username and ID as password.");
    }

    // Remove a student by ID
    public void removeStudent(String studentId) {
        Student student = getStudentById(studentId);

        if (student == null) {
            System.out.println("No student found with ID: " + studentId);
            return;
        }

        // Remove student from all courses they're involved in
        for (Course course : courses) {
            if (course.getEnrolledStudents().contains(student)) {
                course.removeStudent(student);
                System.out.println("Removed student " + student.getName() + " from enrolled list in course " + course.getCourseName());
            }

            // Remove student from waitlist if they were on it
            if (course.getWaitlist().contains(student)) {
                course.removeFromWaitlist(student);
                System.out.println("Removed student " + student.getName() + " from waitlist in course " + course.getCourseName());
            }

            // Remove student from priority queue if they were on it
            if (course.getPriorityQueue().contains(student)) {
                course.removeFromPriorityQueue(student);
                System.out.println("Removed student " + student.getName() + " from priority queue in course " + course.getCourseName());
            }
        }

        // Remove student from the students list
        students.remove(student);
        saveStudentsToFile();
        saveCoursesToFile(); // Save courses to ensure changes persist

        System.out.println("Student with ID " + studentId + " has been removed successfully.");
    }


    // Modify a student's information by ID
    public void modifyStudent(String studentId, String newName) {
        Student student = getStudentById(studentId);

        if (student == null) {
            System.out.println("No student found with ID: " + studentId);
            return;
        }

        // Proceed with modification if the student exists
        student.setName(newName);
        saveStudentsToFile();
        System.out.println("Student with ID " + studentId + " has been updated successfully.");
    }

    // Add this new method to check prerequisites
    private boolean prerequisiteExists(String prerequisiteId) {
        for (Course course : courses) {
            if (course.getCourseId().equals(prerequisiteId)) {
                return true;
            }
        }
        return false;
    }

    // Modified addCourse method
    public void addCourse(Course course) {
        // Check if all prerequisites exist
        LinkedList<String> prerequisites = course.getPrerequisites();
        LinkedList<String> nonExistentPrereqs = new LinkedList<>();

        if (prerequisites != null && !prerequisites.isEmpty()) {
            for (String prerequisiteId : prerequisites) {
                if (!prerequisiteExists(prerequisiteId)) {
                    nonExistentPrereqs.add(prerequisiteId);
                }
            }

            if (!nonExistentPrereqs.isEmpty()) {
                System.out.println("\nWARNING: The following prerequisites have not been created yet:");
                for (String prereq : nonExistentPrereqs) {
                    System.out.println("- " + prereq);
                }

                System.out.print("\nWould you like to continue adding this course anyway? (yes/no): ");
                Scanner scanner = new Scanner(System.in);
                String response = scanner.nextLine().trim().toLowerCase();

                if (!response.equals("yes")) {
                    System.out.println("Course addition cancelled. Please create the prerequisites first.");
                    return;
                }
                System.out.println("Course added with non-existent prerequisites. Please ensure to create them later.");
            }
        }

        courses.add(course);
        saveCoursesToFile();
        System.out.println("Course " + course.getCourseId() + " added successfully.");
    }

    // Remove course by ID
    public void removeCourse(String courseId) {
        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " does not exist. Cannot remove.");
            return;
        }

        // Check if there are students enrolled or on waitlist/priority queue
        if (!course.getEnrolledStudents().isEmpty() || !course.getWaitlist().isEmpty() || !course.getPriorityQueue().isEmpty()) {
            System.out.println("Cannot remove course " + courseId + " as it has students enrolled or waiting for enrollment.");
            System.out.println("Please wait until the course is completely empty before removing.");
            return;
        }

        // Check if this course is a prerequisite for any other courses
        List<Course> dependentCourses = new ArrayList<>();
        for (Course otherCourse : courses) {
            if (otherCourse.getPrerequisites() != null &&
                    otherCourse.getPrerequisites().contains(courseId)) {
                dependentCourses.add(otherCourse);
            }
        }

        if (!dependentCourses.isEmpty()) {
            System.out.println("\nWARNING: Course " + courseId + " is a prerequisite for the following courses:");
            for (Course dependentCourse : dependentCourses) {
                System.out.println("- " + dependentCourse.getCourseId() + ": " + dependentCourse.getCourseName());
            }

            // Ask for confirmation
            System.out.println("\nAre you sure you want to remove this course? (yes/no)");
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine().trim().toLowerCase();

            if (!response.equals("yes")) {
                System.out.println("Course removal cancelled.");
                return;
            }
        }

        // If we get here, either there are no prerequisites or user confirmed removal
        courses.remove(course);
        saveCoursesToFile();
        System.out.println("Course " + courseId + " removed successfully.");
    }

    // Modify a course by ID
    public void modifyCourse(String courseId, String newName, int newCredits, int newCapacity, LinkedList<String> newPrerequisites) {
        Course course = findCourseById(courseId); // Check if the course exists
        if (course == null) {
            System.out.println("Course with ID " + courseId + " does not exist. Cannot modify.");
            return;
        }

        // If course exists, proceed with modification
        course.setCourseName(newName);
        course.setCredits(newCredits);
        course.setCapacity(newCapacity);
        course.setPrerequisites(newPrerequisites);
        saveCoursesToFile(); // Save changes to file
        System.out.println("Course details updated successfully.");
    }


    // View details of a specific course by ID
    public void viewCourseDetails(String courseId) {
        Course course = findCourseById(courseId);
        if (course != null) {
            System.out.println("\nCourse ID: " + course.getCourseId());
            System.out.println("Course Name: " + course.getCourseName());
            System.out.println("Credits: " + course.getCredits());
            System.out.println("Capacity: " + course.getCapacity());
            System.out.println("Prerequisites: " + course.getPrerequisites());

            System.out.println("Enrolled Students:");
            if (course.getEnrolledStudents().isEmpty()) {
                System.out.println("  None");
            } else {
                for (Student student : course.getEnrolledStudents()) {
                    System.out.println("  - ID: " + student.getId() + ", Name: " + student.getName() +
                            ", Enrolled Courses: " + student.getEnrolledCourses());
                }
            }

            System.out.println("Waitlist:");
            if (course.getWaitlist().isEmpty()) {
                System.out.println("  None");
            } else {
                for (Student student : course.getWaitlist()) {
                    System.out.println("  - ID: " + student.getId() + ", Name: " + student.getName() +
                            ", Enrolled Courses: " + student.getEnrolledCourses());
                }
            }
        } else {
            System.out.println("Course with ID " + courseId + " not found.");
        }
    }

    // New method to allow a student to enroll themselves in a course
    public void enrollSelfInCourse(String studentId, String courseId) {
        // Check if the student exists
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }

        // Check if the course exists
        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        // Check if the student has already completed the course with a passing grade
        if (student.getCompletedCourses().contains(courseId)) {
            System.out.print("You have already completed this course with a passing grade. Are you sure you want to retake it? (yes/no): ");
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine().trim().toLowerCase();

            if (!response.equals("yes")) {
                System.out.println("Enrollment canceled. You have already passed this course.");
                return;
            }
        }

        // Check if the student is already enrolled in the course
        if (course.getEnrolledStudents().contains(student)) {
            System.out.println("You are already enrolled in " + course.getCourseName() + ".");
            return;
        }

        // Check if the student is on the waitlist
        if (course.getWaitlist().contains(student)) {
            System.out.println("You are already on the waitlist for " + course.getCourseName() + ".");
            return;
        }

        // Check if the student is in the priority queue
        if (course.getPriorityQueue().contains(student)) {
            System.out.println("You are already in the priority queue for " + course.getCourseName() + ".");
            return;
        }

        // Check if prerequisites are met
        if (!student.hasCompletedCourses(new LinkedList<>(course.getPrerequisites()))) {
            System.out.println("You do not meet the prerequisites for " + course.getCourseName() + ".");
            return;
        }

        // Determine if the student needs the course as a prerequisite for a future course
        boolean isPriority = needsCourseForPrerequisite(student, courseId);
        student.setPriority(isPriority);

        boolean enrolled = course.enrollStudent(student, isPriority);
        if (enrolled) {
            student.addCourse(courseId);
            registrationHistory.push(studentId + "-" + courseId); // Record the action for undo
            saveRegistrationHistory(); // Save registration history to file
            System.out.println(student.getName() + " has been enrolled in " + course.getCourseName());
        } else {
            System.out.println(student.getName() + " has been added to the " +
                    (isPriority ? "priority queue" : "waitlist") + " for " + course.getCourseName());
        }
        student.addCourseToHistory(courseId);
        saveCoursesToFile();
        saveStudentsToFile();
    }

    // Method to retrieve the student ID based on the username
    public String getStudentIdByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getRole().equals("student")) {
                return user.getPassword(); // Return the student's ID, which is stored as their password
            }
        }
        System.out.println("No student ID found for the provided username.");
        return null;
    }

    // Method to view enrolled courses for a student
    public void viewEnrolledCourses(String studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            System.out.println("Enrolled Courses for " + student.getName() + ":");
            if (student.getEnrolledCourses().isEmpty()) {
                System.out.println("No enrolled courses.");
            } else {
                for (String courseId : student.getEnrolledCourses()) {
                    Course course = findCourseById(courseId);
                    if (course != null) {
                        System.out.println(" - " + course.getCourseName() + " (" + course.getCourseId() + ")");
                    } else {
                        System.out.println(" - Course ID " + courseId + " (details not available)");
                    }
                }
            }
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }

    // Method to view completed courses for a student
    public void viewCompletedCourses(String studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            System.out.println("Completed Courses for " + student.getName() + ":");
            if (student.getCompletedCourses().isEmpty()) {
                System.out.println("No completed courses.");
            } else {
                for (String courseId : student.getCompletedCourses()) {
                    Course course = findCourseById(courseId);
                    if (course != null) {
                        System.out.println(" - " + course.getCourseName() + " (" + course.getCourseId() + ")");
                    } else {
                        System.out.println(" - Course ID " + courseId + " (details not available)");
                    }
                }
            }
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }

    // Method to view personal details for a student
    public void viewPersonalDetails(String studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            System.out.println("Personal Details for " + student.getName() + ":");
            System.out.println(" - ID: " + student.getId());
            System.out.println(" - Name: " + student.getName());
            System.out.println(" - Enrolled Courses: " + student.getEnrolledCourses().size());
            System.out.println(" - Completed Courses: " + student.getCompletedCourses().size());
            System.out.println(" - Failed Courses: " + student.getFailedCourses().size());
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }

    // Method to view all courses where student is on waitlist and their position
    public void viewStudentWaitlistPositions(String studentId) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        boolean foundAny = false;
        System.out.println("\nWaitlist Positions:");

        for (Course course : courses) {
            Queue<Student> waitlist = course.getWaitlist();
            int position = 1;
            boolean found = false;

            for (Student waitlistedStudent : waitlist) {
                if (waitlistedStudent.getId().equals(studentId)) {
                    System.out.println("Course: " + course.getCourseName() + " (" + course.getCourseId() + ")");
                    System.out.println("Position on waitlist: " + position);
                    System.out.println("Total students on waitlist: " + waitlist.size());
                    System.out.println("-----------------------------");
                    found = true;
                    foundAny = true;
                    break;
                }
                position++;
            }
        }

        if (!foundAny) {
            System.out.println("You are not on any waitlists.");
        }
    }

    // Method to view all courses where student is in priority queue and their position
    public void viewStudentPriorityQueuePositions(String studentId) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        boolean foundAny = false;
        System.out.println("\nPriority Queue Positions:");

        for (Course course : courses) {
            PriorityQueue<Student> priorityQueue = course.getPriorityQueue();
            // Convert priority queue to array to check positions without removing elements
            Student[] queueArray = priorityQueue.toArray(new Student[0]);

            for (int i = 0; i < queueArray.length; i++) {
                if (queueArray[i].getId().equals(studentId)) {
                    System.out.println("Course: " + course.getCourseName() + " (" + course.getCourseId() + ")");
                    System.out.println("Position in priority queue: " + (i + 1));
                    System.out.println("Total students in priority queue: " + priorityQueue.size());
                    System.out.println("-----------------------------");
                    foundAny = true;
                }
            }
        }

        if (!foundAny) {
            System.out.println("You are not in any priority queues.");
        }
    }

    // Remove a student from a course
    public void removeCourseFromStudent(String studentId, String courseId, boolean isAdmin) {
        Student student = getStudentById(studentId);
        Course course = findCourseById(courseId);

        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }

        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        // Check if the student is enrolled
        if (!course.getEnrolledStudents().contains(student)) {
            System.out.println("Student is not enrolled in this course.");
            return;
        }

        // Remove the student from the course
        if (course.removeEnrolledStudent(student, isAdmin)) {
            student.removeCourse(courseId); // Update the removed student's record

            // Inform admin or user about successful removal
            if (isAdmin) {
                System.out.println("Successfully removed " + student.getName() + " from " + course.getCourseName());
            } else {
                System.out.println("Successfully removed from " + course.getCourseName());
            }

            // Update the next student who is newly enrolled in the course
            // Check the last student in the enrolled list
            if (!course.getEnrolledStudents().isEmpty()) {
                Student nextStudent = course.getEnrolledStudents().getLast(); // Assume newly added is last
                if (!nextStudent.getEnrolledCourses().contains(courseId)) {
                    nextStudent.addCourse(courseId); // Add course to their record
                    System.out.println(nextStudent.getName() + " has been enrolled in " + course.getCourseName());
                }
            }

            // Save updates to files
            saveCoursesToFile();
            saveStudentsToFile();
        } else {
            System.out.println("Failed to remove student from course.");
        }
    }

    // Enroll a student in a course, checking prerequisites and using priority if necessary
    public void enrollStudentInCourse(String studentId, String courseId) {
        Student student = getStudentById(studentId);
        Course course = findCourseById(courseId);
        Scanner scanner = new Scanner(System.in);


        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }

        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        // Check if the student has already completed the course with a passing grade
        if (student.getCompletedCourses().contains(courseId)) {
            System.out.print("The student has already completed this course with a passing grade. Are you sure you want to re-enroll the student? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (!response.equals("yes")) {
                System.out.println("Enrollment canceled. The student has already passed this course.");
                return;
            }
        }

        // Check if the student is already enrolled in the course
        if (course.getEnrolledStudents().contains(student)) {
            System.out.println("The student is already enrolled in " + course.getCourseName() + ".");
            return;
        }

        // Check if the student is on the waitlist
        if (course.getWaitlist().contains(student)) {
            System.out.println("The student is already on the waitlist for " + course.getCourseName() + ".");
            return;
        }

        // Check if the student is in the priority queue
        if (course.getPriorityQueue().contains(student)) {
            System.out.println("The student is already in the priority queue for " + course.getCourseName() + ".");
            return;
        }

        // Check if the student has completed the prerequisites
        if (!student.hasCompletedCourses(new LinkedList<>(course.getPrerequisites()))) {
            System.out.println(student.getName() + " does not meet the prerequisites for " + course.getCourseName() + ".");
            return;
        }

        // Determine if the student needs the course as a prerequisite for a future course
        boolean isPriority = needsCourseForPrerequisite(student, courseId);
        student.setPriority(isPriority);

        boolean enrolled = course.enrollStudent(student, isPriority);
        if (enrolled) {
            student.addCourse(courseId);
            registrationHistory.push(studentId + "-" + courseId); // Record the action for undo
            saveRegistrationHistory(); // Save registration history to file
            System.out.println(student.getName() + " has been enrolled in " + course.getCourseName());
        } else {
            System.out.println(student.getName() + " has been added to the " +
                    (isPriority ? "priority queue" : "waitlist") + " for " + course.getCourseName());
        }

        saveCoursesToFile();
        saveStudentsToFile();
    }

    // Undo the last registration action for a specific student
    public void undoLastRegistrationForStudent(String studentId) {
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        String courseId = student.undoLastCourseRegistration();
        if (courseId == null) {
            System.out.println("No recent registration to undo for the student with ID " + studentId + ".");
            return;
        }
        Course course = findCourseById(courseId);
        if (course != null) {
            // Remove the student from the course if enrolled
            if (course.getEnrolledStudents().contains(student)) {
                course.removeStudent(student);
                student.removeCourse(courseId);
            }

            // Remove the student from the waitlist if present
            if (course.getWaitlist().contains(student)) {
                course.removeFromWaitlist(student);
            }

            // Remove the student from the priority queue if present
            if (course.getPriorityQueue().contains(student)) {
                course.removeFromPriorityQueue(student);
            }// Update student record

            System.out.println("Undid the registration for " + student.getName() + " in course " + course.getCourseName() + " completed.");

            // Check and move the next eligible student from the priority queue or waitlist
            if (!course.getPriorityQueue().isEmpty()) {
                Student nextInPriority = course.getPriorityQueue().poll();
                course.enrollStudent(nextInPriority, true);
            } else if (!course.getWaitlist().isEmpty()) {
                Student nextInWaitlist = course.getWaitlist().poll();
                course.enrollStudent(nextInWaitlist, false);
            }

            saveCoursesToFile();
            saveStudentsToFile();
        } else {
            System.out.println("Course not found.");
        }
    }


    // Undo the last registration action
    public void undoLastRegistration() {
        if (registrationHistory.isEmpty()) {
            System.out.println("No registration actions to undo.");
            return;
        }

        String lastAction = registrationHistory.pop();
        String[] parts = lastAction.split("-");
        String studentId = parts[0];
        String courseId = parts[1];

        Student student = getStudentById(studentId);
        Course course = findCourseById(courseId);

        if (student != null && course != null) {
            // Remove the student from the course and their list of enrolled courses
            course.removeStudent(student);
            student.removeCourse(courseId);

            System.out.println("Undid the registration of " + student.getName() + " from " + course.getCourseName());

            // Save changes to ensure they persist after the program restarts
            saveRegistrationHistory();
            saveCoursesToFile();
            saveStudentsToFile();
        }
    }

    public void handleCourseCompletion(String studentId, String courseId, String grade) {
        Student student = getStudentById(studentId);
        if (student != null) {
            // Check if the grade is a passing grade
            if (!grade.equals("F")) {
            //student.completeCourse(courseId); // Move course to completed if passed
                System.out.println("Course " + courseId + " marked as completed for student " + studentId);
            }
            notifyCourseCompletion(courseId);  // Notify for any waitlist processing
        }
    }


    public void notifyCourseCompletion(String courseId) {
        checkAndEnrollNextStudentInCourse(courseId);
    }

    // Method to check and enroll the next student in line when a spot opens up
    public void checkAndEnrollNextStudentInCourse(String courseId) {
        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        Student enrolledStudent = null;

        // Check the priority queue first
        if (!course.getPriorityQueue().isEmpty()) {
            Student nextInPriority = course.getPriorityQueue().poll();
            course.enrollStudent(nextInPriority, true);
            nextInPriority.addCourse(courseId);
            System.out.println("Enrolled " + nextInPriority.getName() + " from the priority queue for course " + course.getCourseName());
        }
        // If priority queue is empty, check the waitlist
        else if (!course.getWaitlist().isEmpty()) {
            Student nextInWaitlist = course.getWaitlist().poll();
            course.enrollStudent(nextInWaitlist, false);
            nextInWaitlist.addCourse(courseId);
            System.out.println("Enrolled " + nextInWaitlist.getName() + " from the waitlist for course " + course.getCourseName());
        }

        if (enrolledStudent != null) {
            // Enroll the student in the course
            course.enrollStudent(enrolledStudent, false);
            enrolledStudent.addCourse(courseId);

            // Update the course waitlist and priority queue data files
            course.saveWaitlist();
            course.savePriorityQueue();

            // Save the updated course and student information
            saveCoursesToFile();
            saveStudentsToFile();
        } else {
            System.out.println("No students in the priority queue or waitlist for course " + course.getCourseName());
        }
    }

    public void loadRegistrationHistory() {
        File file = new File(registrationHistoryFilePath);
        if (!file.exists()) {
            System.out.println("No previous registration history found. Starting fresh.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(registrationHistoryFilePath))) {
            registrationHistory = (Stack<String>) ois.readObject();
            System.out.println("Registration history loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No registration history found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Method to load users from file
    private void loadUsersFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userFilePath))) {
            users = (LinkedList<User>) ois.readObject();
            System.out.println("User data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("User data file not found, starting with an empty user list.");
            createDefaultUsers();
            saveUsersToFile();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

// Enter grades for students in a course
public void enterGradesForCourse(String courseId, Scanner scanner) {
    Course course = findCourseById(courseId);
    if (course == null) {
        System.out.println("Course with ID " + courseId + " not found.");
        return;
    }

    LinkedList<Student> enrolledStudents = new LinkedList<>(course.getEnrolledStudents());

    for (Student student : enrolledStudents) {
        System.out.print("Enter grade (A+, A, A-, B+, B, B-, C+, C, F) for " + student.getName() + " (ID: " + student.getId() + "): ");
        String grade = scanner.nextLine().toUpperCase();

        if (!isValidGrade(grade)) {
            System.out.println("Invalid grade entered. Please enter a valid grade (A+, A, A-, B+, B, B-, C+, C, F).");
            return;
        }

        saveGrade(student.getId(), courseId, grade);
        System.out.println("Grade " + grade + " set for student " + student.getId() + " in course " + courseId);
        handleCourseCompletion(student.getId(), courseId, grade);

        if (!grade.equals("F")) {
            student.completeCourse(courseId);
            course.getEnrolledStudents().remove(student);
            System.out.println(student.getName() + " has completed the course " + courseId);
        } else {
            failCourse(student.getId(), courseId);
        }

        checkAndEnrollNextStudentInCourse(courseId);
    }

    System.out.println("All grades have been entered successfully.");
}

    // Method to view the failed courses of a student
    public void viewFailedCoursesForStudent(String studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            System.out.println("Failed Courses for " + student.getName() + ":");
            if (student.getFailedCourses().isEmpty()) {
                System.out.println("No failed courses.");
            } else {
                for (String courseId : student.getFailedCourses()) {
                    Course course = findCourseById(courseId);
                    if (course != null) {
                        System.out.println(" - " + course.getCourseName() + " (" + course.getCourseId() + ")");
                    } else {
                        System.out.println(" - Course ID " + courseId + " (details not available)");
                    }
                }
            }
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }

    // Fail the course for a student
    public void failCourse(String studentId, String courseId) {
        // Get the student by ID
        Student student = getStudentById(studentId);
        if (student == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return;
        }

        // Find the course by ID
        Course course = findCourseById(courseId);
        if (course == null) {
            System.out.println("Course with ID " + courseId + " not found.");
            return;
        }

        // Remove the student from the enrolled list
        if (course.getEnrolledStudents().contains(student)) {
            course.getEnrolledStudents().remove(student);
            System.out.println(student.getName() + " has been removed from the enrolled list in course " + courseId);
        }

        // Add the student to the failed courses list
        student.failCourse(courseId);
        System.out.println(student.getName() + " has been added to the failed courses list for course " + courseId);

        // Optionally, save the changes if required
        saveStudentsToFile();  // Save student data
        saveCoursesToFile();  // Save course data
    }


    // View grades for a specific course
    public void viewGradesForCourse(String courseId) {
        File gradeFile = new File("grades.csv");

        // Check if the file exists
        if (!gradeFile.exists()) {
            System.out.println("Error: Grade file not found. Please ensure the grade CSV file has been created.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("grades.csv"))) {
            String line;
            System.out.println("Grades for course: " + courseId);
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[1].equals(courseId)) {
                    System.out.println(" - Student ID: " + parts[0] + ", Grade: " + parts[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void viewGradesForStudent(String studentId) {
        File gradeFile = new File("grades.csv");

        // Check if the file exists
        if (!gradeFile.exists()) {
            System.out.println("No grades are available to be displayed.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("grades.csv"))) {
            String line;
            boolean hasGrades = false;
            System.out.println("Grades for student ID: " + studentId);
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(studentId)) {
                    System.out.println(" - Course ID: " + parts[1] + ", Grade: " + parts[2]);
                    hasGrades = true;
                }
            }
            // Check if no grades were found for the student
            if (!hasGrades) {
                System.out.println("No grades found for student ID: " + studentId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// Modify a student's grade for a specific course
public void modifyGradeForStudent(String studentId, String courseId, String newGrade) {
    Student student = getStudentById(studentId);
    if (student == null) {
        System.out.println("Student with ID " + studentId + " not found.");
        return;
    }

    // Define the path to the grade CSV file
    String gradeFilePath = "grades.csv"; // Adjust this path to match your file structure

    // Check if the CSV file exists
    File gradeFile = new File(gradeFilePath);
    if (!gradeFile.exists()) {
        System.out.println("Error: Grade file not found. Please ensure the grade CSV file has been created.");
        return;
    }

    // Check if the course exists
    Course course = findCourseById(courseId);
    if (course == null) {
        System.out.println("Course with ID " + courseId + " not found.");
        return;
    }

    // Check if the student is either enrolled or has completed the course
    boolean isEnrolled = course.getEnrolledStudents().contains(student);
    boolean hasCompleted = student.getCompletedCourses().contains(courseId);
    boolean hasFailed = student.getFailedCourses().contains(courseId);

    if (!isEnrolled && !hasCompleted && !hasFailed) {
        System.out.println("Student with ID " + studentId + " is neither enrolled nor has completed the course " + courseId + ".");
        return;
    }

    // Prompt the user to enter a valid grade since the checks have passed
    if (!isValidGrade(newGrade)) {
        System.out.println("Invalid grade entered. Please enter a valid grade (A+, A, A-, B+, B, B-, C+, C, F).");
        return;
    }

    // Modify the grade in the CSV file
    modifyGradeInCSV(studentId, courseId, newGrade);

    // Update the student's completed and failed courses lists based on the new grade
    if (newGrade.equals("F")) {
        // If changing to F, remove from completed and add to failed
        student.failCourse(courseId);
        student.getCompletedCourses().remove(courseId);
    } else if (hasCompleted) {
        // If it's already in completed courses and new grade is passing,
        // just update the grade without adding duplicate entry
        modifyGradeInCSV(studentId, courseId, newGrade);
    } else {
        // If it was previous;y failed or new enrollment
        student.completeCourse(courseId);
        student.getFailedCourses().remove(courseId);
    }

    // Save the updated student and course data to files
    saveStudentsToFile();
    saveCoursesToFile();

    System.out.println("Grade updated successfully for student ID " + studentId + " in course " + courseId);
}

    // Method to check if a grade is valid
    private boolean isValidGrade(String grade) {
        return grade.matches("A\\+|A|A-|B\\+|B|B-|C\\+|C|F");
    }

    public void saveGrade(String studentId, String courseId, String grade) {
        try (FileWriter writer = new FileWriter("grades.csv", true)) { // true to append
            writer.write(studentId + "," + courseId + "," + grade + "\n");
            System.out.println("Grade saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to modify an existing grade in the CSV file
    private void modifyGradeInCSV(String studentId, String courseId, String newGrade) {
        File tempFile = new File("temp_grades.csv");
        File originalFile = new File("grades.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
             FileWriter writer = new FileWriter(tempFile)) {

            String line;
            boolean gradeUpdated = false;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(studentId) && parts[1].equals(courseId)) {
                    // Write the updated grade
                    writer.write(studentId + "," + courseId + "," + newGrade + "\n");
                    gradeUpdated = true;
                } else {
                    // Write the original line
                    writer.write(line + "\n");
                }
            }

            if (!gradeUpdated) {
                System.out.println("Grade entry not found for the specified student and course.");
            }
            System.out.println("Grade modified successfully in file.");

        } catch (IOException e) {
            System.out.println("Error updating grade file.");
            e.printStackTrace();
        }
        // Replace the old file with the updated one
        if (!originalFile.delete()) {
            System.out.println("Could not delete original file.");
        } else if (!tempFile.renameTo(originalFile)) {
            System.out.println("Could not rename temporary file.");
        }
    }

    // Method to save users to file
    private void saveUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userFilePath))) {
            oos.writeObject(users);
            System.out.println("User data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultUsers() {
        users.add(new User("Shanaldo Carty", "Cinnamon Bun", "admin"));
    }

    // Method to authenticate user by iterating through the LinkedList
    public boolean authenticateUser(String username, String password, String role) {
        for (User user : users) {
            if (user.getUsername().equals(username) &&
                    user.getPassword().equals(password) &&
                    user.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    // Method to add a new user (for admin use or adding a student with ID as password)
    public void addUser(String username, String password, String role) {
        // Check if user already exists to avoid duplicates
        boolean exists = users.stream().anyMatch(user -> user.getUsername().equals(username));
        if (!exists) {
            users.add(new User(username, password, role));
            saveUsersToFile();
            System.out.println("User added successfully with username: " + username + " and ID as password.");
        }
    }

    // Find a course by ID using the linked list
    public Course findCourseById(String courseId) {
        for (Course course : courses) {
            if (course.getCourseId().equals(courseId)) {
                return course;
            }
        }
        return null;
    }

    // Get a student by ID
    public Student getStudentById(String studentId) {
        for (Student student : students) {
            if (student.getId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }

    // Method to display detailed information of a specific student
    public void viewStudentDetails(String studentId) {
        Student student = getStudentById(studentId);
        if (student != null) {
            System.out.println("\nStudent ID: " + student.getId());
            System.out.println("Student Name: " + student.getName());
            System.out.println("Enrolled Courses: " + student.getEnrolledCourses());
            System.out.println("Completed Courses: " + student.getCompletedCourses());
            System.out.println("Failed Courses: " + student.getFailedCourses());
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }

    private boolean needsCourseForPrerequisite(Student student, String courseId) {
        for (Course course : courses) {
            LinkedList<String> prerequisites = course.getPrerequisites();
            // Check if the course has the given courseId as a prerequisite and if the student hasn't completed the course
            if (prerequisites.contains(courseId) && !student.hasCompletedCourses(prerequisites)) {
                return true;
            }
        }
        return false;
    }

    // Load student data from a file
    private void loadStudentsFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(studentFilePath))) {
            students = (LinkedList<Student>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No previous student data found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Save student data to a file
    private void saveStudentsToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(studentFilePath))) {
            oos.writeObject(students);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load course data from a file
    private void loadCoursesFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(courseFilePath))) {
            courses = (LinkedList<Course>) ois.readObject();
            System.out.println("Course data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No previous course data found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Save course data to a file
    private void saveCoursesToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(courseFilePath))) {
            oos.writeObject(courses);
            System.out.println("Course data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to remove a student from the waitlist of a specific course
    public void removeStudentFromWaitlist(String studentId, String courseId) {
        Course course = findCourseById(courseId);
        if (course != null) {
            Student student = getStudentById(studentId);
            if (student != null) {
                if (course.removeFromWaitlist(student)) {
                    course.saveWaitlist(); // Save changes to file immediately
                    saveCoursesToFile(); // Save courses to maintain overall consistency
                    System.out.println("Student " + student.getName() + " removed from waitlist for course " + course.getCourseName());
                } else {
                    System.out.println("Student is not on the waitlist for this course.");
                }
            } else {
                System.out.println("Student not found.");
            }
        } else {
            System.out.println("Course not found.");
        }
    }

    // Method to remove a student from the priority queue of a specific course
    public void removeStudentFromPriorityQueue(String studentId, String courseId) {
        Course course = findCourseById(courseId);
        if (course != null) {
            Student student = getStudentById(studentId);
            if (student != null) {
                if (course.removeFromPriorityQueue(student)) {
                    course.savePriorityQueue(); // Save changes to file immediately
                    saveCoursesToFile(); // Save courses to maintain overall consistency
                    System.out.println("Student " + student.getName() + " removed from priority queue for course " + course.getCourseName());
                } else {
                    System.out.println("Student is not in the priority queue for this course.");
                }
            } else {
                System.out.println("Student not found.");
            }
        } else {
            System.out.println("Course not found.");
        }
    }

    // Method to display the waitlist for a specific course
    public void viewWaitlistByCourseId(String courseId) {
        Course course = findCourseById(courseId);
        if (course != null) {
            System.out.println("Waitlist for course " + course.getCourseName() + " (" + course.getCourseId() + "):");
            if (course.getWaitlist().isEmpty()) {
                System.out.println("No students on the waitlist.");
            } else {
                for (Student student : course.getWaitlist()) {
                    System.out.println(" - ID: " + student.getId() + ", Name: " + student.getName());
                }
            }
        } else {
            System.out.println("Course with ID " + courseId + " not found.");
        }
    }

    // Method to display the priority queue for a specific course
    public void viewPriorityListByCourseId(String courseId) {
        Course course = findCourseById(courseId);
        if (course != null) {
            System.out.println("Priority Queue for course " + course.getCourseName() + " (" + course.getCourseId() + "):");
            if (course.getPriorityQueue().isEmpty()) {
                System.out.println("No students in the priority queue.");
            } else {
                for (Student student : course.getPriorityQueue()) {
                    System.out.println(" - ID: " + student.getId() + ", Name: " + student.getName());
                }
            }
        } else {
            System.out.println("Course with ID " + courseId + " not found.");
        }
    }

    // Display all courses
    public void displayAllCourses() {
        if (courses.isEmpty()) {
            System.out.println("No courses available to display.");
            return;
        }

        for (Course course : courses) {
            System.out.println(course);
        }
    }

    // Display all students
    public void displayAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No students available to display.");
            return;
        }

        for (Student student : students) {
            System.out.println(student);
        }
    }
}