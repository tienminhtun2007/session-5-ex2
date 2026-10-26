package com.example.session3.modals.services;

import com.example.session3.dto.CourseCreateRequest;
import com.example.session3.dto.CourseResponse;
import com.example.session3.dto.CourseUpdateRequest;
import com.example.session3.modals.entities.Course;
import com.example.session3.modals.entities.Instructors;
import com.example.session3.modals.repositories.CourseRepository;
import com.example.session3.modals.repositories.InstructorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.session3.dto.PageResponse;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, InstructorRepository instructorRepository) {
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy khóa học với ID: " + id));
    }

    public Course createCourse(CourseCreateRequest request) {
        Instructors instructor = instructorRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy giảng viên với ID: " + request.getInstructorId()));
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setStatus(request.getStatus());
        course.setInstructor(instructor);
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, CourseUpdateRequest request) {
        Course existing = getCourseById(id);
        Instructors instructor = instructorRepository.findById(request.getInstructorId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy giảng viên với ID: " + request.getInstructorId()));
        existing.setTitle(request.getTitle());
        existing.setStatus(request.getStatus());
        existing.setInstructor(instructor);
        return courseRepository.save(existing);
    }

    public Course deleteCourseById(Long id) {
        Course existing = getCourseById(id);
        courseRepository.delete(existing);
        return existing;
    }

    public PageResponse<CourseResponse> getPagedCourses(int page, int size, String sortBy, Sort.Direction direction) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        String sortField = (sortBy == null || sortBy.trim().isEmpty()) ? "id" : sortBy.trim();
        Sort.Direction sortDirection = (direction == null) ? Sort.Direction.DESC : direction;
        Sort sort = Sort.by(sortDirection, sortField);
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<Course> coursePage = courseRepository.findAll(pageable);
        Page<CourseResponse> responsePage = coursePage.map(course -> new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getStatus(),
                course.getInstructor() != null ? course.getInstructor().getId() : null,
                course.getInstructor() != null ? course.getInstructor().getName() : null));
        return new PageResponse<>(
                responsePage.getContent(),
                responsePage.getNumber(),
                responsePage.getSize(),
                responsePage.getTotalElements(),
                responsePage.getTotalPages(),
                responsePage.isLast());
    }

}
