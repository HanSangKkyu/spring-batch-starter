package com.example.batchdemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//
//@Entity
//@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

//    @Id
    Long id;

//    @Column(name = "name")
    String name;

}
