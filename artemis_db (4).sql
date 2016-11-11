-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Apr 06, 2016 at 06:38 AM
-- Server version: 5.6.17
-- PHP Version: 5.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `artemis_db`
--
CREATE DATABASE IF NOT EXISTS `artemis_db` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `artemis_db`;

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `addInstitute`(IN `id` VARCHAR(100), IN `des` TEXT, IN `color` VARCHAR(255), IN `status` VARCHAR(200))
BEGIN
insert into institutes values(id,des,color,status);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addRoom`(IN `id` VARCHAR(100), IN `d` TEXT, IN `inst_id` VARCHAR(100), IN `status` VARCHAR(200))
BEGIN
insert into rooms values(id,d,inst_id,status);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addSchedule`(IN `st` TIME, IN `et` TIME, IN `days` VARCHAR(255), IN `section` VARCHAR(10), IN `sbj_id` VARCHAR(255), IN `room_id` VARCHAR(255), IN `teacher_id` VARCHAR(255), IN `inst_id` VARCHAR(100), IN `sy` VARCHAR(100))
BEGIN
insert into class_sched values(null,st,et,days,section,sbj_id,room_id,
teacher_id,inst_id,sy);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addSchoolYear`(IN `sy` VARCHAR(255), IN `status` VARCHAR(200))
BEGIN
insert into school_year values(sy,status);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addSubject`(IN `id` VARCHAR(100), IN `dc` TEXT, IN `lec` TINYINT, IN `lab` TINYINT, IN `acc_stat` VARCHAR(100), IN `pf` VARCHAR(100))
BEGIN
insert into subjects values(id,dc,lec,lab,acc_stat,pf);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addTeacher`(IN `id` VARCHAR(255),IN `fn` VARCHAR(255), IN `ln` VARCHAR(255), IN `gender` VARCHAR(255), IN `inst_id` VARCHAR(100), IN `acc_stat` VARCHAR(100))
BEGIN
insert into teachers values(id,fn,ln,gender,acc_stat,inst_id);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addUserAccount`(firstname varchar(255),
lastname varchar(255),gender varchar(10),op varchar(100),inst varchar(100),
access varchar(100),un varchar(255),pw varchar(255))
BEGIN
insert into user_accounts values(null,firstname,lastname,gender,op,inst,access,un,pw);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `dayConflicts`(rid varchar(100),st time,et time,school_year varchar(100))
BEGIN
#params(room,st,et,sy)
#this procedure shows the schedule with conflicts to the parameter
select * from class_sched 
where room_id = rid and !(st <= start_time and et <= start_time or
 st >= end_time and et >= end_time) and sy = school_year;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getRoomClassSched`(IN `r_id` VARCHAR(100), IN `d` VARCHAR(100))
BEGIN
select * from class_sched
where room_id = r_id and `day` = d;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `showActiveSchoolYear`()
BEGIN
select * from school_year where access_status = 'Active';
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `showAllInstitutes`()
BEGIN
select * from institutes;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `showAllPrograms`()
SELECT * FROM programs$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `showAllRooms`()
BEGIN
select * from rooms
order by id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `showAllSchoolYear`()
BEGIN
select * from school_year;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `showAllSubjects`()
BEGIN
select * from subjects;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `showAllTeachers`()
BEGIN
select * from teachers;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `showAllUserAccounts`()
BEGIN
 select id,firstname,lastname,gender,access_type,institute,access_status,username from user_accounts;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `showRoomsByInstitute`(i varchar(255))
BEGIN
select * from rooms
where institute_id = i
order by id;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `showTeachersPopulation`(IN `gen` VARCHAR(100))
SELECT institute_id,count(id)as count,gender FROM teachers
WHERE gender= gen
GROUP BY institute_id$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `class_sched`
--

CREATE TABLE IF NOT EXISTS `class_sched` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `day` varchar(255) NOT NULL,
  `section` varchar(10) NOT NULL,
  `subject_id` varchar(100) NOT NULL,
  `room_id` varchar(100) NOT NULL,
  `teacher_id` varchar(255) NOT NULL,
  `institute_id` varchar(100) NOT NULL,
  `sy` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `institute_id` (`institute_id`),
  KEY `room_id` (`room_id`),
  KEY `subject_id` (`subject_id`),
  KEY `teacher_id` (`teacher_id`),
  KEY `sy` (`sy`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=26 ;

--
-- Dumping data for table `class_sched`
--

INSERT INTO `class_sched` (`id`, `start_time`, `end_time`, `day`, `section`, `subject_id`, `room_id`, `teacher_id`, `institute_id`, `sy`) VALUES
(4, '10:00:00', '11:00:00', 'Mon', 'B', 'IT114', 'CL1', '2', 'ICE', '2015-2016 1st Semester'),
(5, '10:00:00', '11:00:00', 'Wed', 'B', 'IT114', 'CL1', '2', 'ICE', '2015-2016 1st Semester'),
(6, '11:30:00', '13:00:00', 'Mon', 'B', 'IT123', 'CL2', '2', 'ICE', '2015-2016 1st Semester'),
(7, '21:00:00', '22:00:00', 'Wed', 'B', 'IT123', 'CL2', '2', 'ICE', '2015-2016 1st Semester'),
(9, '07:00:00', '07:30:00', 'Mon', 'A1', 'IT158', 'CL1', '123421-123', 'ICE', '2015-2016 1st Semester'),
(10, '07:00:00', '07:30:00', 'Wed', 'A1', 'IT158', 'CL1', '123421-123', 'ICE', '2015-2016 1st Semester'),
(11, '07:00:00', '07:30:00', 'Fri', 'A1', 'IT158', 'CL1', '123421-123', 'ICE', '2015-2016 1st Semester'),
(12, '07:30:00', '09:00:00', 'Mon', 'A1', 'IT114', 'CL1', '34234-234', 'ICE', '2015-2016 1st Semester'),
(13, '07:30:00', '09:00:00', 'Wed', 'A1', 'IT114', 'CL1', '34234-234', 'ICE', '2015-2016 1st Semester'),
(14, '07:30:00', '09:00:00', 'Fri', 'A1', 'IT114', 'CL1', '34234-234', 'ICE', '2015-2016 1st Semester'),
(15, '09:00:00', '10:00:00', 'Mon', 'A1', 'LEA2', 'CL1', '13', 'IBPA', '2015-2016 1st Semester'),
(16, '09:00:00', '10:00:00', 'Wed', 'A1', 'LEA2', 'CL1', '13', 'IBPA', '2015-2016 1st Semester'),
(17, '07:00:00', '07:30:00', 'Mon', 'A1', 'HUM1', 'AG102', '4', 'IALS', '2015-2016 1st Semester'),
(18, '07:00:00', '07:30:00', 'Wed', 'A1', 'HUM1', 'AG102', '4', 'IALS', '2015-2016 1st Semester'),
(19, '07:00:00', '07:30:00', 'Fri', 'A1', 'HUM1', 'AG102', '4', 'IALS', '2015-2016 1st Semester'),
(20, '07:30:00', '09:00:00', 'Mon', 'A1', 'HUM1', 'AG102', '7', 'IALS', '2015-2016 1st Semester'),
(21, '07:30:00', '09:00:00', 'Wed', 'A1', 'HUM1', 'AG102', '7', 'IALS', '2015-2016 1st Semester'),
(22, '07:30:00', '09:00:00', 'Fri', 'A1', 'HUM1', 'AG102', '7', 'IALS', '2015-2016 1st Semester'),
(23, '09:00:00', '10:00:00', 'Mon', 'A1', 'EDUC114', 'AG102', '11', 'IETT', '2015-2016 1st Semester'),
(24, '09:00:00', '10:00:00', 'Wed', 'A1', 'EDUC114', 'AG102', '11', 'IETT', '2015-2016 1st Semester'),
(25, '09:00:00', '10:00:00', 'Fri', 'A1', 'EDUC114', 'AG102', '11', 'IETT', '2015-2016 1st Semester');

-- --------------------------------------------------------

--
-- Table structure for table `institutes`
--

CREATE TABLE IF NOT EXISTS `institutes` (
  `id` varchar(100) NOT NULL,
  `description` text,
  `color` varchar(255) DEFAULT NULL,
  `access_status` varchar(200) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `institutes`
--

INSERT INTO `institutes` (`id`, `description`, `color`, `access_status`) VALUES
('IALS', 'Institute of Agriculture and Life Sciences', '0:204:102', 'Active'),
('IBPA', 'Institute of Business and Public Affairs', '255:51:51', 'Active'),
('ICE', 'Intstitute of Computing and Engineering', '255:153:51', 'Active'),
('IETT', 'Institute of Education and Teachers Training', '0:102:255', 'Active');

-- --------------------------------------------------------

--
-- Table structure for table `programs`
--

CREATE TABLE IF NOT EXISTS `programs` (
  `id` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `access_status` varchar(255) NOT NULL,
  `facilitator` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `facilitator` (`facilitator`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `rooms`
--

CREATE TABLE IF NOT EXISTS `rooms` (
  `id` varchar(100) NOT NULL,
  `description` text,
  `institute_id` varchar(100) NOT NULL,
  `access_status` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `institute_id` (`institute_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `rooms`
--

INSERT INTO `rooms` (`id`, `description`, `institute_id`, `access_status`) VALUES
('AG101', 'Agriculture Room', 'IALS', 'Active'),
('AG102', 'Agriculture Room', 'IALS', 'Active'),
('CL1', 'Computer Lab 1', 'ICE', 'Active'),
('CL2', 'Computer Lab 2', 'ICE', 'Active'),
('CL3', 'Comp Lab 3', 'ICE', 'Active'),
('CL4', 'Comp Lab 4', 'ICE', 'Active'),
('CL5', 'Comp Lab 5', 'ICE', 'Active'),
('ED101', 'Eudcation 101', 'IBPA', 'Active'),
('G4', 'Gym 4', 'IBPA', 'Draft'),
('H2', 'Hrm 2', 'IBPA', 'Archived'),
('H7', 'Hotel 7', 'IBPA', 'Active'),
('H8', 'Hrm 8', 'IBPA', 'Archived'),
('ML1', 'Multimedia Lab 1', 'ICE', 'Active'),
('MS1', '<Unidentified Yet>', 'IALS', 'Active'),
('MS2', 'Ambot', 'IALS', 'Active');

-- --------------------------------------------------------

--
-- Table structure for table `school_year`
--

CREATE TABLE IF NOT EXISTS `school_year` (
  `sy_sem` varchar(255) NOT NULL,
  `access_status` varchar(200) NOT NULL,
  PRIMARY KEY (`sy_sem`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `school_year`
--

INSERT INTO `school_year` (`sy_sem`, `access_status`) VALUES
('2015-2016 1st Semester', 'Active');

-- --------------------------------------------------------

--
-- Table structure for table `subjects`
--

CREATE TABLE IF NOT EXISTS `subjects` (
  `id` varchar(100) NOT NULL,
  `description` text,
  `lect_units` tinyint(4) DEFAULT NULL,
  `lab_units` tinyint(4) DEFAULT NULL,
  `access_status` varchar(100) NOT NULL,
  `program_facilitator` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `program_facilitator` (`program_facilitator`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `subjects`
--

INSERT INTO `subjects` (`id`, `description`, `lect_units`, `lab_units`, `access_status`, `program_facilitator`) VALUES
('EDUC114', 'Principles of Learning', 4, 0, 'Active', 'IETT'),
('HUM1', 'Humanities 101', 3, 0, 'Active', 'IALS'),
('IT100', 'esvsebsrbs', 3, 0, 'Active', 'ICE'),
('IT101', 'wefwef', 3, 3, 'Active', 'ICE'),
('IT114', 'afs', 3, 4, 'Draft', 'ICE'),
('IT115', 'wefnwle', 3, 0, 'Active', 'ICE'),
('IT123', 'asfsf', 3, 3, 'Active', 'ICE'),
('IT158', 'Mobile Programming', 3, 3, 'Active', 'ICE'),
('LEA2', 'ambot', 3, 3, 'Active', 'IBPA');

-- --------------------------------------------------------

--
-- Table structure for table `teachers`
--

CREATE TABLE IF NOT EXISTS `teachers` (
  `id` varchar(255) NOT NULL,
  `firstname` varchar(255) NOT NULL,
  `lastname` varchar(255) NOT NULL,
  `gender` varchar(10) NOT NULL,
  `access_status` varchar(100) NOT NULL,
  `institute_id` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `institute_id` (`institute_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `teachers`
--

INSERT INTO `teachers` (`id`, `firstname`, `lastname`, `gender`, `access_status`, `institute_id`) VALUES
('1', 'Dony', 'Dongiapon', 'Male', 'Active', 'ICE'),
('10', 'Jay Paul', 'Gonzaga', 'Male', 'Archived', 'ICE'),
('11', 'Jun-jun', 'Negro', 'Male', 'Active', 'IETT'),
('12', 'Rodrigo', 'Duterte', 'Male', 'Active', 'IETT'),
('123421-123', 'Jefferson', 'Buot', 'Male', 'Active', 'ICE'),
('13', 'Marumi', 'Roxas', 'Male', 'Active', 'IBPA'),
('14', 'Gracia', 'Poe', 'Female', 'Active', 'IALS'),
('2', 'Lanie', 'Laureano', 'Female', 'Archived', 'ICE'),
('23452-235', 'Lyza', 'Soberano', 'Female', 'Active', 'ICE'),
('3', 'Don John Jays', 'Dela Cruz', 'Male', 'Active', 'IBPA'),
('34234-234', 'Julius', 'Ambot', 'Male', 'Active', 'ICE'),
('3efwe', 'fwefw', 'wef', 'Male', 'Active', 'ICE'),
('4', 'Donyarita Maria Josifenawwegweg', 'Tagapagtanggols', 'Female', 'Active', 'IALS'),
('5', 'Idonu', 'Watosay', 'Male', 'Active', 'IBPA'),
('6', 'Jean', 'Eballe', 'Female', 'Active', 'ICE'),
('7', 'Wail', 'He', 'Male', 'Draft', 'IALS'),
('8', 'Jeah', 'Gonzales', 'Male', 'Active', 'IETT'),
('9', 'Dianne', 'Reyes', 'Female', 'Active', 'IBPA'),
('kj', 'kjh', 'kj', 'Male', 'Active', 'ICE');

-- --------------------------------------------------------

--
-- Table structure for table `user_accounts`
--

CREATE TABLE IF NOT EXISTS `user_accounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `firstname` varchar(255) NOT NULL,
  `lastname` varchar(255) NOT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `access_type` varchar(100) DEFAULT NULL,
  `institute` varchar(100) NOT NULL,
  `access_status` varchar(100) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `user_password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9 ;

--
-- Dumping data for table `user_accounts`
--

INSERT INTO `user_accounts` (`id`, `firstname`, `lastname`, `gender`, `access_type`, `institute`, `access_status`, `username`, `user_password`) VALUES
(1, 'Jefferson', 'Buot', 'Male', 'Institute Admin', 'ICE', 'Active', 'jeff', 'qvcqczohs'),
(2, 'Neilza', 'Buot', 'Female', 'Institute Admin', 'IBPA', 'Draft', 'jez', 'qvcqczohs'),
(3, 'Soliven Jess', 'Montillado', 'Male', 'View Only', 'N/A', 'Active', 'soliven_25', 'acbhwzzorc'),
(4, 'Chuckie', 'Gumobao', 'Male', 'View Only', 'N/A', 'Active', 'chuckie', 'qviqywsuom'),
(5, 'Rey', 'Sumaylo', 'Male', 'View Only', 'N/A', 'Active', 'CoolDev', 'fsmgiaomzc'),
(6, 'User', 'User', 'Male', 'View Only', 'N/A', 'Active', 'user', 'doggkcfr'),
(7, 'Jestony', 'Pagayon', 'Male', 'Institute Admin', 'ICE', 'Active', 'Jestony', 'xsg041789'),
(8, 'Blythe', 'Lobrigas', 'Male', 'Institute Admin', 'IBPA', 'Active', 'blythe', 'qodgzcqy2293');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `class_sched`
--
ALTER TABLE `class_sched`
  ADD CONSTRAINT `class_sched_ibfk_1` FOREIGN KEY (`institute_id`) REFERENCES `institutes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `class_sched_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `class_sched_ibfk_3` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `class_sched_ibfk_4` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `class_sched_ibfk_5` FOREIGN KEY (`sy`) REFERENCES `school_year` (`sy_sem`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `programs`
--
ALTER TABLE `programs`
  ADD CONSTRAINT `programs_ibfk_1` FOREIGN KEY (`facilitator`) REFERENCES `institutes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `rooms`
--
ALTER TABLE `rooms`
  ADD CONSTRAINT `rooms_ibfk_1` FOREIGN KEY (`institute_id`) REFERENCES `institutes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `subjects`
--
ALTER TABLE `subjects`
  ADD CONSTRAINT `subjects_ibfk_1` FOREIGN KEY (`program_facilitator`) REFERENCES `institutes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `teachers`
--
ALTER TABLE `teachers`
  ADD CONSTRAINT `teachers_ibfk_1` FOREIGN KEY (`institute_id`) REFERENCES `institutes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
