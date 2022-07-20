CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `username` varchar(20) DEFAULT NULL,
                         `email` varchar(50) DEFAULT NULL,
                         `password` varchar(120) DEFAULT NULL,
                         `name` varchar(100) NOT NULL,
                         `age` int NOT NULL,
                         `creation_time` datetime DEFAULT NULL,
                         `modified_time` datetime DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
                         UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `roles` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `name` varchar(20) NOT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `user_roles` (
                              `user_id` bigint NOT NULL,
                              `role_id` bigint NOT NULL,
                              PRIMARY KEY (`user_id`,`role_id`),
                              KEY `fk_users_has_roles_users_idx` (`role_id`),
                              CONSTRAINT `fk_users_has_roles_roles1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                              CONSTRAINT `fk_users_has_roles_users` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
