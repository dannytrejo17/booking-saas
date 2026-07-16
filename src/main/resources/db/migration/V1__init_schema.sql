-- MySQL dump 10.13  Distrib 8.4.4, for Win64 (x86_64)
--
-- Host: localhost    Database: saas_booking
-- ------------------------------------------------------
-- Server version	8.4.4

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `booking`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `customer_name` varchar(255) DEFAULT NULL,
  `customer_phone` varchar(255) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `business_id` bigint DEFAULT NULL,
  `employee_id` bigint DEFAULT NULL,
  `service_id` bigint DEFAULT NULL,
  `start_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKobmwu8k3vy9wipq6ts6hteggf` (`business_id`),
  KEY `FK1dnnhqt4wl3v6a72hxiarf7lg` (`employee_id`),
  KEY `FKcebnlefwi9r13txu8btclnmsu` (`service_id`),
  CONSTRAINT `FK1dnnhqt4wl3v6a72hxiarf7lg` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `FKcebnlefwi9r13txu8btclnmsu` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`),
  CONSTRAINT `FKobmwu8k3vy9wipq6ts6hteggf` FOREIGN KEY (`business_id`) REFERENCES `business` (`business_id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `business`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business` (
  `business_id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `logo` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `slug` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `owner_id` bigint DEFAULT NULL,
  PRIMARY KEY (`business_id`),
  KEY `FKb4maq7rjfml4gm6ydg4djjyb6` (`owner_id`),
  CONSTRAINT `FKb4maq7rjfml4gm6ydg4djjyb6` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `business_schedule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `business_id` bigint NOT NULL,
  `day_of_week` varchar(10) NOT NULL,
  `open_time` time(6) NOT NULL,
  `close_time` time(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_slot` (`business_id`,`day_of_week`,`open_time`,`close_time`),
  CONSTRAINT `fk_schedule_business` FOREIGN KEY (`business_id`) REFERENCES `business` (`business_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `employee`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `business_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKby2mxoe9cvixkcsckxqom6wft` (`business_id`),
  CONSTRAINT `FKby2mxoe9cvixkcsckxqom6wft` FOREIGN KEY (`business_id`) REFERENCES `business` (`business_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `employee_schedule`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `close_time` time(6) DEFAULT NULL,
  `day_of_week` enum('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') DEFAULT NULL,
  `open_time` time(6) DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKopf7kqhml8g3efddqc6rki7jv` (`employee_id`),
  CONSTRAINT `FKopf7kqhml8g3efddqc6rki7jv` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `service`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `duration` int NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `business_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK22lnxqxohc8ofdjmfl0qcanoj` (`business_id`),
  CONSTRAINT `FK22lnxqxohc8ofdjmfl0qcanoj` FOREIGN KEY (`business_id`) REFERENCES `business` (`business_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `business_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4xigqfhyelwj2n5psmxapdr0o` (`business_id`),
  CONSTRAINT `FK4xigqfhyelwj2n5psmxapdr0o` FOREIGN KEY (`business_id`) REFERENCES `business` (`business_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-16 17:27:42
