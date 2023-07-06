package com.vladimirpandurov.securecapita02B.dtoMapper;

import com.vladimirpandurov.securecapita02B.domain.Role;
import com.vladimirpandurov.securecapita02B.domain.User;
import com.vladimirpandurov.securecapita02B.dto.UserDTO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class UserDTOMapper {

    public static UserDTO fromUser(User user){
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    public static UserDTO fromUser(User user, Role role){
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        userDTO.setRoleName(role.getName());
        userDTO.setPermissions(role.getPermission());
        return userDTO;
    }

    public static User toUser(UserDTO userDTO){
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        return user;
    }

}
