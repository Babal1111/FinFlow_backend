package com.example.finflow.auth_service.service;

import com.example.finflow.auth_service.entity.User;
import com.example.finflow.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    public final UserRepository userRepository;

    @Autowired
    public final PasswordEncoder passwordEncoder;


    public User register(User user){
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if(existingUser.isPresent()){
            User oldUSer = existingUser.get();

            // The Optional.get() method returns the value inside the Optional if it is present.
            // However, it is generally considered risky because if you call get() without first checking with isPresent(),
            // you might run into a noSuch element exception
            oldUSer.setName(user.getName());
            oldUSer.setEmail(user.getEmail());
            return oldUSer;
        }else{
            System.out.println("before hashing");
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
            return userRepository.save(user);
        }

    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User not found"));
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public User updateUser(Long id,User newUser){
        //User oldUser = userRepository.findById(id);
        //findById() Returns an Optional<User>:
        User oldUser = getUserById(id);
        oldUser.setName(newUser.getName());
        oldUser.setEmail(newUser.getEmail());
        return userRepository.save(oldUser) ;

    }
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }




}
