package com.example.finflow.auth_service.service;

import com.example.finflow.auth_service.dto.AuthResponseDto;
import com.example.finflow.auth_service.dto.LoginRequestDto;
import com.example.finflow.auth_service.dto.RegisterRequestDto;
import com.example.finflow.auth_service.dto.UserResponseDto;
import com.example.finflow.auth_service.entity.User;
import com.example.finflow.auth_service.repository.UserRepository;
import com.example.finflow.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    private final JwtUtil jwtUtil;

    public AuthResponseDto register(RegisterRequestDto req){
        Optional<User> existingUser = userRepository.findByEmail(req.getEmail());
        if(existingUser.isPresent()){
            User oldUSer = existingUser.get();

            oldUSer.setName(req.getName());
            oldUSer.setEmail(req.getEmail());
            userRepository.save(oldUSer);  // IMP Save the updated user

            String token = jwtUtil.generateToken(oldUSer.getEmail(), oldUSer.getRole().name());
            System.out.println("Token genrated after reg: "+token);
            return new AuthResponseDto(
                    req.getName(),
                    req.getEmail(),
                    token,
                    req.getRole().name(),
                    "User already existed, updation successful");
        }else{
            System.out.println("before hashing");
            String hashedPassword = passwordEncoder.encode(req.getPassword());
            req.setPassword(hashedPassword);
            User user = new User();
            user.setName(req.getName());
            user.setEmail(req.getEmail());
            user.setPassword(req.getPassword());  // password is already hashed
            user.setRole(req.getRole());
            userRepository.save(user);

            userRepository.save(user);  // Save the new user to the database
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            System.out.println("Token genrated after reg: "+token);
            return new AuthResponseDto(
                    req.getName(),
                    req.getEmail(),
                    token,
                    req.getRole().name(),
                    " New Registration successful");
        }

    }
    public List<UserResponseDto> getAllUsers() {
        List<User> users =  userRepository.findAll();

        return users.stream()
                .map(user->new UserResponseDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
                ))
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long id){

        User user = userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User not found"));
        UserResponseDto dto = modelMapper.map(user,UserResponseDto.class);
        //ModelMapper won’t convert Enum --> String automatically
        dto.setRole(user.getRole().name());
        return  dto;

    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public UserResponseDto updateUser(Long id, User newUser) {
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!oldUser.getEmail().equals(newUser.getEmail()) && userRepository.existsByEmail(newUser.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        oldUser.setName(newUser.getName());
        oldUser.setEmail(newUser.getEmail());

        if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
            oldUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }

        userRepository.save(oldUser);
        UserResponseDto dto = modelMapper.map(oldUser,UserResponseDto.class);
        dto.setRole(oldUser.getRole().name());
        return dto;
    }
    public UserResponseDto getUserByEmail(String email) {
                User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponseDto dto = modelMapper.map(user,UserResponseDto.class);
        dto.setRole(user.getRole().name());
        return dto;
    }

    public AuthResponseDto login(LoginRequestDto req){
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(()-> new RuntimeException("User not found with this email"));

        if(!passwordEncoder.matches(req.getPassword(), user.getPassword())){
            throw new RuntimeException("INVALID  PASSWORD");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponseDto(
                user.getName(),
                user.getEmail(),
                token,
                user.getRole().name(),
                "Login successful");     // JWT later

    }




}
