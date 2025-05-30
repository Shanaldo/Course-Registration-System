//Author: Shanaldo Carty
//Completed Date: Pending,2025

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.io.FileInputStream;     // For reading files
import java.io.FileOutputStream;    // For writing files
import java.io.ObjectInputStream;   // For deserializing objects
import java.io.ObjectOutputStream;  // For serializing objects
import java.io.FileNotFoundException; // To handle missing files
import java.io.IOException;         // To handle I/O errors

public class Course implements Serializable {
    private static final long serialVersionUID = 1L;
    private String courseId;
    private String courseName;
    private int credits;
    private int capacity;
    private LinkedList<Student> enrolledStudents;
    private Queue<Student> waitlist;
    private PriorityQueue<Student> priorityQueue; // New attribute for the priority queue
    private LinkedList<String> prerequisites;


    // Inner class for custom Comparator to sort student by ID in the PriorityQueue that is Serializable
    private static class StudentComparator implements Comparator<Student>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Student s1, Student s2) {
            return s1.getId().compareTo(s2.getId());
        }
    }

    public Course(String courseId, String courseName, int credits, int capacity, LinkedList<String> prerequisites) {
        // Validate courseId format (3 uppercase letters followed by 4 digits)
        if (!courseId.matches("[A-Z]{3}\\d{4}")) {
            throw new IllegalArgumentException("Course ID must be exactly 3 uppercase letters followed by 4 digits (e.g., CMP1001).");
        }
        this.courseId = courseId;

        // Set course name and validate that it start with a capital letter
        setCourseName(courseName);

        // Validate credits are positive
        if (credits < 0) {
            throw new IllegalArgumentException("Credits must be a positive number.");
        }
        this.credits = credits;

        // Validate capacity is positive
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive number.");
        }
        this.capacity = capacity;

        // Initialize lists and queues for student management
        this.enrolledStudents = new LinkedList<>();
        this.waitlist = new LinkedList<>();
        this.priorityQueue = new PriorityQueue<>(new StudentComparator());
        this.prerequisites = prerequisites != null ? prerequisites : new LinkedList<>();

        // Load persistent data for waitlist and priority queue
        loadWaitlist();
        loadPriorityQueue();
    }

    // Load the priority queue from a file specific to this course and creates a new queue of no file is found.
    public void loadPriorityQueue() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("priorityQueue_" + courseId + ".dat"))) {
            priorityQueue = (PriorityQueue<Student>) ois.readObject();
            System.out.println("Priority queue loaded for course " + courseId);
        } catch (FileNotFoundException e) {
            System.out.println("No priority queue found for course " + courseId + ". Starting fresh.");
            priorityQueue = new PriorityQueue<>(new StudentComparator());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Save the priority queue to a file for future retrieval
    public void savePriorityQueue() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("PriorityQueue_" + courseId + ".dat"))) {
            oos.writeObject(priorityQueue);
            System.out.println("Priority queue saved for course " + courseId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load the waitlist from a file specific to this course and creates a new waitlist if no file is found
    public void loadWaitlist() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("WaitList_" + courseId + ".dat"))) {
            waitlist = (Queue<Student>) ois.readObject();
            System.out.println("Waitlist loaded for course " + courseId);
        } catch (FileNotFoundException e) {
            System.out.println("No waitlist found for course " + courseId + ". Starting fresh.");
            waitlist = new LinkedList<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Save the waitlist to a file for future retrieval
    public void saveWaitlist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("waitlist_" + courseId + ".dat"))) {
            oos.writeObject(waitlist);
            System.out.println("Waitlist saved for course " + courseId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters and setters with validation
    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    // Sets the course name with validation to ensure it starts with a capital letter.
    public void setCourseName(String courseName) {
        if (courseName == null || !courseName.matches("[A-Z][a-zA-Z\\s]*")) {
            throw new IllegalArgumentException("Course name must start with a capital letter.");
        }
        this.courseName = courseName;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        if (credits < 0) {
            throw new IllegalArgumentException("Credits must be a positive number.");
        }
        this.credits = credits;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive number.");
        }
        this.capacity = capacity;
    }

    public LinkedList<String> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(LinkedList<String> prerequisites) {
        this.prerequisites = prerequisites;
    }

    // Enroll a student in the course
    public boolean enrollStudent(Student student, boolean isPriority) {
        // Check if the student is already enrolled
        if (enrolledStudents.contains(student)) {
            System.out.println(student.getName() + " is already enrolled in " + courseName + ".");
            return false;
        }

        // Enroll student if capacity allows
        if (enrolledStudents.size() < capacity) {
            enrolledStudents.add(student);
            System.out.println(student.getName() + " has been successfully enrolled in " + courseName + ".");
            return true;
        }

        // Add to the priority queue if necessary
        if (isPriority) {
            priorityQueue.add(student);
            System.out.println(student.getName() + " has been added to the priority queue for " + courseName + ".");
            savePriorityQueue();
        } else {
            addToWaitlist(student);
            saveWaitlist();
        }

        return false; // Student not enrolled, added to queue instead
    }

    // Check if a student is already on the waitlist
    private boolean isStudentOnWaitlist(Student student) {
        return waitlist.stream().anyMatch(s -> s.getId().equals(student.getId()));
    }

    // Add a student to the waitlist
    private void addToWaitlist(Student student) {
        if (!isStudentOnWaitlist(student)) {
            waitlist.add(student);
            System.out.println(student.getName() + " has been added to the waitlist for " + courseName + ".");
            saveWaitlist();
        }
    }

    public void removeStudent(Student student) {
        if (enrolledStudents.remove(student)) {
            // If there are students in the priority queue, add them first
            if (!priorityQueue.isEmpty()) {
                Student nextInLine = priorityQueue.poll();
                enrolledStudents.add(nextInLine);
                nextInLine.addCourse(this.courseId); // Add course to student's enrolled courses
                System.out.println(nextInLine.getName() + " has been enrolled from the priority queue.");
                savePriorityQueue();
            }
            // If no priority students, add from the waitlist
            else if (!waitlist.isEmpty()) {
                Student nextInLine = waitlist.poll();
                enrolledStudents.add(nextInLine);
                nextInLine.addCourse(this.courseId); // Add course to student's enrolled courses
                System.out.println(nextInLine.getName() + " has been enrolled from the waitlist.");
                saveWaitlist();
            }
        }
    }

    // Method to remove a student from the waitlist
    public boolean removeFromWaitlist(Student student) {
        if (waitlist.remove(student)) {
            System.out.println(student.getName() + " has been removed from the waitlist for " + courseName + ".");
            saveWaitlist();
            return true;
        } else {
            System.out.println(student.getName() + " is not on the waitlist for " + courseName + ".");
            return false;
        }
    }

    // Method to remove a student from the priority queue
    public boolean removeFromPriorityQueue(Student student) {
        if (priorityQueue.remove(student)) {
            System.out.println(student.getName() + " has been removed from the priority queue for " + courseName + ".");
            savePriorityQueue();
            return true;
        } else {
            System.out.println(student.getName() + " is not in the priority queue for " + courseName + ".");
            return false;
        }
    }

    public boolean removeEnrolledStudent(Student student, boolean isAdmin) {
        if (enrolledStudents.contains(student)) {
            enrolledStudents.remove(student);
            // Check and move students from priority queue or waitlist
            if (!priorityQueue.isEmpty()) {
                Student nextInLine = priorityQueue.poll();
                enrolledStudents.add(nextInLine);
                nextInLine.addCourse(this.courseId); // Add course to student's enrolled list
                if (isAdmin) {
                    System.out.println(nextInLine.getName() + " has been enrolled from the priority queue.");
                }
                savePriorityQueue();
            } else if (!waitlist.isEmpty()) {
                Student nextInLine = waitlist.poll();
                enrolledStudents.add(nextInLine);
                nextInLine.addCourse(this.courseId); // Add course to student's enrolled list
                if (isAdmin) {
                    System.out.println(nextInLine.getName() + " has been enrolled from the waitlist.");
                }
                saveWaitlist();
            }
            return true;
        }
        return false;
    }

    // Get the list of enrolled students
    public LinkedList<Student> getEnrolledStudents() {
        return enrolledStudents;
    }

    // Get the waitlist
    public Queue<Student> getWaitlist() {
        return waitlist;
    }

    // Get the priority queue
    public PriorityQueue<Student> getPriorityQueue() {
        return priorityQueue;
    }

    @Override
    public String toString() {
        return "\nCourse ID: " + courseId + ", Course Name: " + courseName +
                ", Credits: " + credits + ", Capacity: " + capacity +
                ", Prerequisites: " + prerequisites +
                ", Enrolled Students: " + enrolledStudents.size() +
                ", Waitlist: " + waitlist.size() +
                ", Priority Queue: " + priorityQueue.size();
    }
}
