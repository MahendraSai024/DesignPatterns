package com.designpatterns.builder;


import lombok.Setter;

class Student{
    String name; // Mandatory
    int age;
    String address;
    float wallet;

    // Multiple Constructors
    public Student(String Name){}
    public Student(String Name, int age){}

    private Student(StudentBuilder builder){
        this.name = builder.name;
        this.age = builder.age;
        this.address = builder.address;
        this.wallet = builder.wallet;
    }

    // Inner - Builder class
    @Setter
    static class StudentBuilder{
        // Has access to the methods
        String name; // Mandatory
        int age;
        String address;
        float wallet;
        public StudentBuilder(String name){ // All mandatory fields has to be set now
            this.name = name;
        }

        StudentBuilder setAge(int age){
            this.age = age;
            return this; // TO further build on top of this
        }

        StudentBuilder setAddress(String address){
            this.address = address;
            return this;
        }

        StudentBuilder setWallet(float wallet){
            this.wallet = wallet;
            return this;
        }

        // Build Method = to build the final method
        Student build(){
            return new Student(this);
        }
    }
}

public class BuilderPattern {

    public static void main(String[] args) {
        Student student = new Student.StudentBuilder("Mahendra").setAge(22).build();

        System.out.println(student.age + student.name + student.wallet + student.address);
    }
}


