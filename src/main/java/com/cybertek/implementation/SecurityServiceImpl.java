package com.cybertek.implementation;

import com.cybertek.dto.UserDTO;
import com.cybertek.entity.User;
import com.cybertek.entity.common.UserPrincipal;
import com.cybertek.mapper.UserMapper;
import com.cybertek.repository.UserRepository;
import com.cybertek.service.SecurityService;
import com.cybertek.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityServiceImpl implements SecurityService {

    private UserService userService;
    private UserMapper userMapper;

    public SecurityServiceImpl(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        UserDTO user = userService.findByUserName(s);
        if(user==null){
            throw new UsernameNotFoundException("This user does not exist");
        }

        return new org.springframework.security.core.userdetails.User(user.getId().toString(),
                user.getPassWord(), listAuthorities(user));
    }

    @Override
    public User loadUser(String param) {
        UserDTO user = userService.findByUserName(param);
        return userMapper.convertToEntity(user);
    }

    private Collection<? extends GrantedAuthority> listAuthorities(UserDTO user){
        List<GrantedAuthority> authorityList = new ArrayList<>();

        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getDescription());
        authorityList.add(authority);

        return authorityList;
    }
}
