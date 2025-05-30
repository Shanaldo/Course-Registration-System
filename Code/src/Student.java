//Author: Shanaldo Carty
//Completed Date: Pending,2025

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Stack;


public class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private LinkedList<String> enrolledCourses;
    private LinkedList<String> completedCourses; // Tracks courses the student has completed
    private LinkedList<String> failedCourses; // Tracks courses the student has failed
    private boolean priority; // Indicates if the student has priority for a course
    private Stack<String> registrationHistory = new Stack<>();

    public Student(String id, String name) {
        setId(id); // Validate and set student ID
        setName(name); // Validate and set student name
        this.enrolledCourses = new LinkedList<>();
        this.completedCourses = new LinkedList<>();
        this.failedCourses = new LinkedList<>(); // Initialize failed courses list
        this.priority = false; // Default priority to false
    }

    // Getters and setters with validation
    public String getId() {
        return id;
    }

    public void setId(String id) {
        // Validate that the student ID is exactly 7 digits
        if (id == null || !id.matches("\\d{7}")) {
            throw new IllegalArgumentException("Student ID must be exactly 7 digits.");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        // Validate that the name contains a first and last name, each starting with a capital letter
        if (name == null || !name.matches("[A-Z][a-zA-Z]+ [A-Z][a-zA-Z]+")) {
            throw new IllegalArgumentException("Student name must contain a first and last name, each starting with a capital letter (e.g., John Doe).");
        }
        this.name = name;
    }

    // Add a course to enrolled courses
    public void addCourse(String courseId) {
        if (!enrolledCourses.contains(courseId)) {
            enrolledCourses.add(courseId);
        }
    }

    public void addCourseToHistory(String courseId) {
        registrationHistory.push(courseId);
    }

    public String undoLastCourseRegistration() {
        return registrationHistory.isEmpty() ? null : registrationHistory.pop();
    }

    public LinkedList<String> getEnrolledCourses() {
        return enrolledCourses;
    }

    // Mark the course as completed
    public void completeCourse(String courseId) {
        enrolledCourses.remove(courseId);
        completedCourses.add(courseId);
    }

    // Mark the course as failed
    public void failCourse(String courseId) {
        enrolledCourses.remove(courseId);
        failedCourses.add(courseId);
    }

    // Remove a course from the student's enrolled courses list
    public void removeCourse(String courseId) {
        enrolledCourses.remove(courseId);
    }

    // Get list of completed courses
    public LinkedList<String> getCompletedCourses() {
        return completedCourses;
    }

    // Get list of failed courses
    public LinkedList<String> getFailedCourses() {
        return failedCourses;
    }

    // Check if the student has completed all courses in the list of prerequisites
    public boolean hasCompletedCourses(LinkedList<String> requiredCourses) {
        return completedCourses.containsAll(requiredCourses);
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return id != null && id.equals(student.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "\nID: " + id + ", Name: " + name + ", Enrolled Courses: " + enrolledCourses + ", Completed Courses: " + completedCourses + ", Failed Courses: " + failedCourses;
    }
}
