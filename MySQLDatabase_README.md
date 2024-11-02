# MySQL Database - Parking Lot Management System

## Overview This MySQL database supports the Parking Lot Management System by storing user accounts, parking spots, and reservations. It enables real-time updates for managing available and reserved parking spots efficiently.

## Setup Instructions

Create the Database:
First, run the following command in MySQL to create and use the database:
sql
Copy code
CREATE DATABASE ParkingLotDB;
USE ParkingLotDB;
Run SQL Script:
The full database schema and table setup commands are saved in the ParkingLotDB_Setup.sql file within this repository.
To create the necessary tables, simply run the script:
sql
Copy code
SOURCE /path/to/ParkingLotDB_Setup.sql;
(Replace /path/to/ with the actual path to the SQL file.)
Verify Setup:
After running the script, you should see tables for Users, ParkingSpots, and Reservations created in the database.
## Dependencies

MySQL Server installed and running.
Java application connected to the database using MySQL Connector/J.
