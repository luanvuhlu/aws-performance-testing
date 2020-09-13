package com.luanvv.springboot.rest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.luanvv.springboot.rest.entities.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>{

}
