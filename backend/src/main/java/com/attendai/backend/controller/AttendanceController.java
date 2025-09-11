package com.attendai.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendai.backend.dto.AtRiskStudent;
import com.attendai.backend.dto.AttendanceRequest;
import com.attendai.backend.dto.ClassStudentDTO;
import com.attendai.backend.dto.ClassSummary;
import com.attendai.backend.dto.FacultyDashboardDTO;
import com.attendai.backend.dto.GraphData;
import com.attendai.backend.dto.RiskStudentDTO;
import com.attendai.backend.dto.StudentDashboardDTO;
import com.attendai.backend.dto.SubjectAttendance;
import com.attendai.backend.model.Attendance;
import com.attendai.backend.model.ClassSession;
import com.attendai.backend.model.Role;
import com.attendai.backend.model.User;
import com.attendai.backend.repository.AttendanceRepository;
import com.attendai.backend.repository.ClassSessionRepository;
import com.attendai.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:3000")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepo;

    @Autowired
    private ClassSessionRepository sessionRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * Mark attendance for the authenticated student
     * Method: POST /api/attendance/mark
     * Body: { "sessionId": 1, "method": "QR" }
     */
    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(
            @RequestBody AttendanceRequest request) {

        // Get authenticated user (email) from JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentEmail = auth.getName();

        // Fetch user to get roll number
        Optional<User> userOpt = userRepo.findByEmailIgnoreCase(studentEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        String rollNumber = userOpt.get().getRollNumber();

        // Validate session
        Optional<ClassSession> sessionOpt = sessionRepo.findById(request.getSessionId());
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Session not found");
        }
        ClassSession session = sessionOpt.get();

        if (!"ACTIVE".equals(session.getStatus())) {
            return ResponseEntity.badRequest().body("Session is not active");
        }

        // Prevent duplicate marking
        Optional<Attendance> existing = attendanceRepo
                .findByRollNumberAndSessionId(rollNumber, request.getSessionId());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Attendance already marked for Roll No: " + rollNumber);
        }

        // Save new attendance record
        Attendance attendance = new Attendance();
        attendance.setRollNumber(rollNumber);
        attendance.setSessionId(request.getSessionId());
        attendance.setTimestamp(LocalDateTime.now());
        attendance.setPresent(true);
        attendance.setMethod(request.getMethod()); // "QR", "FACIAL", "MANUAL"

        attendanceRepo.save(attendance);

        return ResponseEntity.ok("✅ Attendance marked successfully for Roll No: " + rollNumber);
    }

    /**
     * View own attendance history (Student)
     * GET /api/attendance/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<Attendance>> getMyAttendance() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<User> userOpt = userRepo.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        String rollNumber = userOpt.get().getRollNumber();

        List<Attendance> attendanceList = attendanceRepo.findByRollNumber(rollNumber);
        return ResponseEntity.ok(attendanceList);
    }

    /**
     * View attendance for a session (Faculty only)
     * GET /api/attendance/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSessionAttendance(@PathVariable Long sessionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String facultyEmail = auth.getName();

        Optional<ClassSession> sessionOpt = sessionRepo.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Session not found");
        }

        ClassSession session = sessionOpt.get();
        if (!session.getFacultyEmail().equals(facultyEmail)) {
            return ResponseEntity.status(403).body("❌ Access denied: You are not the faculty for this session");
        }

        List<Attendance> attendanceList = attendanceRepo.findBySessionId(sessionId);
        return ResponseEntity.ok(attendanceList);
    }

    /**
     * View attendance by roll number (Admin/Faculty)
     * GET /api/attendance/student/{rollNumber}
     */
    @GetMapping("/student/{rollNumber}")
    public ResponseEntity<List<Attendance>> getAttendanceByStudent(
            @PathVariable String rollNumber) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepo.findByEmailIgnoreCase(auth.getName()).orElse(null);

        // Only Admin and Faculty can view others' attendance
        if (currentUser != null && "STUDENT".equals(currentUser.getRole())) {
            // Student can only view their own if roll number matches
            String myRollNumber = currentUser.getRollNumber();
            if (!myRollNumber.equals(rollNumber)) {
                return ResponseEntity.status(403).body(null);
            }
        }

        List<Attendance> attendanceList = attendanceRepo.findByRollNumber(rollNumber);
        return ResponseEntity.ok(attendanceList);
    }

            @GetMapping("/analytics/risk-students")
        public ResponseEntity<List<RiskStudentDTO>> getAtRiskStudents() {
            // Get all students
            List<User> students = userRepo.findByRole(Role.STUDENT);
            List<RiskStudentDTO> riskList = new ArrayList<>();

            for (User s : students) {
                List<Attendance> att = attendanceRepo.findByRollNumber(s.getRollNumber());
                if (att.isEmpty()) continue;

                long present = att.stream().filter(Attendance::isPresent).count();
                double percent = (double) present / att.size() * 100;

                if (percent < 75) {
                    RiskStudentDTO dto = new RiskStudentDTO();
                    dto.setRollNumber(s.getRollNumber());
                    dto.setName(s.getName());
                    dto.setOverallAttendance(Math.round(percent * 10) / 10.0);
                    dto.setDepartment(s.getDepartment());
                    riskList.add(dto);
                }
            }

            return ResponseEntity.ok(riskList);
        }
                @GetMapping("/class/students/{sessionId}")
        public ResponseEntity<List<ClassStudentDTO>> getStudentsInClass(@PathVariable Long sessionId) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String facultyEmail = auth.getName();

            // Verify faculty owns the session
            Optional<ClassSession> sessionOpt = sessionRepo.findById(sessionId);
            if (sessionOpt.isEmpty() || !sessionOpt.get().getFacultyEmail().equals(facultyEmail)) {
                return ResponseEntity.status(403).body(null);
            }

            // Get all students in the same department as the session (e.g., CSE)
            ClassSession session = sessionOpt.get();
            List<User> allStudents = userRepo.findByDepartmentAndRole(session.getDepartment(), Role.STUDENT);

            // Get attendance records for this session
            List<Attendance> attendanceList = attendanceRepo.findBySessionId(sessionId);

            // Map attendance by roll number
            Map<String, Attendance> attendanceMap = attendanceList.stream()
                .collect(Collectors.toMap(Attendance::getRollNumber, a -> a));

            // Build response
            List<ClassStudentDTO> students = new ArrayList<>();
            for (User s : allStudents) {
                Attendance att = attendanceMap.get(s.getRollNumber());
                students.add(new ClassStudentDTO(
                    s.getRollNumber(),
                    s.getName(),
                    att != null && att.isPresent(),
                    att != null ? att.getMethod() : "N/A"
                ));
            }

            return ResponseEntity.ok(students);
        }
        @GetMapping("/analytics/graph/weekly")
        public ResponseEntity<GraphData> getWeeklyGraph() {
            // Mock data for last 6 days
            List<String> labels = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
            List<Integer> data = Arrays.asList(92, 88, 76, 90, 85, 70);

            GraphData graph = new GraphData();
            graph.setLabels(labels);
            graph.setData(data);

            return ResponseEntity.ok(graph);
        }
        @GetMapping("/dashboard/faculty")
        public ResponseEntity<FacultyDashboardDTO> getFacultyDashboard() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            Optional<User> userOpt = userRepo.findByEmailIgnoreCase(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            User user = userOpt.get();

            List<ClassSession> todaySessions = sessionRepo.findByFacultyEmailAndStartTimeBetween(
                email, LocalDate.now().atStartOfDay(), LocalDate.now().atTime(23, 59)
            );

            FacultyDashboardDTO dashboard = new FacultyDashboardDTO();
            dashboard.setName(user.getName());
            dashboard.setDepartment(user.getDepartment());
            dashboard.setTodayClasses(todaySessions.stream()
                .map(s -> new ClassSummary(s.getSubject(), s.getStartTime().toString(), 
                        s.getEndTime().toString(), s.getLocation(), s.getStatus()))
                .collect(Collectors.toList()));
            dashboard.setActiveSessions((int) todaySessions.stream().filter(s -> "ACTIVE".equals(s.getStatus())).count());

            // Mock: total attendance today
            dashboard.setTotalAttendanceToday(28);

            // Mock: at-risk students
            List<AtRiskStudent> riskList = Arrays.asList(
                new AtRiskStudent("CSE2025S001", "AI", 72)
            );
            dashboard.setAtRiskStudents(riskList);

            return ResponseEntity.ok(dashboard);
        }
        @GetMapping("/dashboard/student")
        public ResponseEntity<StudentDashboardDTO> getStudentDashboard() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            Optional<User> userOpt = userRepo.findByEmailIgnoreCase(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            User user = userOpt.get();

            // Calculate overall attendance
            List<Attendance> attendanceList = attendanceRepo.findByRollNumber(user.getRollNumber());
            long total = attendanceList.size();
            long present = attendanceList.stream().filter(Attendance::isPresent).count();
            double overall = total == 0 ? 0 : (double) present / total * 100;

            // Mock subject-wise data (you can link to ClassSession)
            List<SubjectAttendance> subjects = Arrays.asList(
                new SubjectAttendance("Artificial Intelligence", 72, "At Risk"),
                new SubjectAttendance("Database Systems", 94, "Good"),
                new SubjectAttendance("Software Engineering", 88, "Good")
            );

            StudentDashboardDTO dashboard = new StudentDashboardDTO();
            dashboard.setName(user.getName());
            dashboard.setRollNumber(user.getRollNumber());
            dashboard.setDepartment(user.getDepartment());
            dashboard.setOverallAttendance(Math.round(overall * 10) / 10.0);
            dashboard.setTotalClasses(total);
            dashboard.setPresent(present);
            dashboard.setAbsent(total - present);
            dashboard.setSubjects(subjects);

            return ResponseEntity.ok(dashboard);
        }
}   