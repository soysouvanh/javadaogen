-- phpMyAdmin SQL Dump
-- version 5.1.1deb5ubuntu1
-- https://www.phpmyadmin.net/
--
-- Hôte : localhost:3306
-- Généré le : mar. 29 avr. 2025 à 12:13
-- Version du serveur : 8.0.41-0ubuntu0.22.04.1
-- Version de PHP : 8.3.20

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `aogo`
--

-- --------------------------------------------------------

--
-- Structure de la table `address`
--

CREATE TABLE `address` (
  `addressId` bigint UNSIGNED NOT NULL COMMENT 'Address identifier',
  `address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Address',
  `place` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Place',
  `zipCode` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Zip code (reference_zipCode)',
  `cityId` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'City identifier (reference_city)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Addresses table.';

-- --------------------------------------------------------

--
-- Structure de la table `customer`
--

CREATE TABLE `customer` (
  `customerId` int UNSIGNED NOT NULL COMMENT 'Customer identifier',
  `customerCode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Customer code',
  `civilityId` tinyint UNSIGNED NOT NULL COMMENT 'Civility identifier',
  `lastName` varchar(48) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Last name',
  `updated` datetime NOT NULL COMMENT 'Updated date',
  `createdDate` date NOT NULL COMMENT 'Date of creation',
  `createdTime` time NOT NULL COMMENT 'Time of creation'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Customers table.';

-- --------------------------------------------------------

--
-- Structure de la table `customer_address`
--

CREATE TABLE `customer_address` (
  `customerId` int NOT NULL COMMENT 'Customer idenfier (customer)',
  `addressId` int NOT NULL COMMENT 'Address identifier (address)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Custoer address relation table.';

-- --------------------------------------------------------

--
-- Structure de la table `customer_authentication`
--

CREATE TABLE `customer_authentication` (
  `customerId` int UNSIGNED NOT NULL COMMENT 'Customer identifier',
  `pseudonym` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Pseudonym',
  `emailId` int UNSIGNED NOT NULL COMMENT 'Email identifier (email)',
  `password` char(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Password hashed in SHA1',
  `active` tinyint UNSIGNED NOT NULL COMMENT 'Active status: active=1, inactive=0',
  `updated` datetime NOT NULL COMMENT 'Updated date'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Customers authentication table.';

-- --------------------------------------------------------

--
-- Structure de la table `email`
--

CREATE TABLE `email` (
  `emailId` int UNSIGNED NOT NULL COMMENT 'Email identifier',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Email'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='E-mails table.';

-- --------------------------------------------------------

--
-- Structure de la table `test`
--

CREATE TABLE `test` (
  `testId` int UNSIGNED NOT NULL COMMENT 'Test identifier',
  `language` char(2) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Language: fr, en, ...',
  `label` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'label',
  `active` tinyint UNSIGNED NOT NULL COMMENT '1: active, 0: inactive'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Tests table.';

--
-- Déchargement des données de la table `test`
--

INSERT INTO `test` (`testId`, `language`, `label`, `active`) VALUES
(1, 'fr', 'Updated Label', 1);

-- --------------------------------------------------------

--
-- Structure de la table `testpkintint`
--

CREATE TABLE `testpkintint` (
  `pk1int` int UNSIGNED NOT NULL COMMENT 'PK 1 int',
  `pk2int` int UNSIGNED NOT NULL COMMENT 'PK 2 int',
  `label` varchar(8) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Label',
  `language` char(2) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Language fr or en',
  `idx12` int UNSIGNED NOT NULL COMMENT 'Index part 1 on 2',
  `idx22` int UNSIGNED NOT NULL COMMENT 'Index part 2 on 2'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='testpkintint table';

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `address`
--
ALTER TABLE `address`
  ADD PRIMARY KEY (`addressId`),
  ADD KEY `zipCode` (`zipCode`),
  ADD KEY `cityId` (`cityId`);

--
-- Index pour la table `customer`
--
ALTER TABLE `customer`
  ADD PRIMARY KEY (`customerId`),
  ADD UNIQUE KEY `customerCode` (`customerCode`),
  ADD KEY `civilityId` (`civilityId`),
  ADD KEY `lastName` (`lastName`),
  ADD KEY `createdDate` (`createdDate`),
  ADD KEY `createdTime` (`createdTime`);

--
-- Index pour la table `customer_address`
--
ALTER TABLE `customer_address`
  ADD UNIQUE KEY `customerId_addressId` (`customerId`,`addressId`) USING BTREE,
  ADD KEY `customerId` (`customerId`),
  ADD KEY `addressId` (`addressId`);

--
-- Index pour la table `customer_authentication`
--
ALTER TABLE `customer_authentication`
  ADD PRIMARY KEY (`customerId`),
  ADD UNIQUE KEY `emailId` (`emailId`) USING BTREE,
  ADD UNIQUE KEY `pseudonym` (`pseudonym`),
  ADD KEY `active` (`active`);

--
-- Index pour la table `email`
--
ALTER TABLE `email`
  ADD PRIMARY KEY (`emailId`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Index pour la table `test`
--
ALTER TABLE `test`
  ADD PRIMARY KEY (`testId`),
  ADD UNIQUE KEY `languageLabel` (`language`,`label`),
  ADD KEY `language` (`language`),
  ADD KEY `label` (`label`),
  ADD KEY `active` (`active`);

--
-- Index pour la table `testpkintint`
--
ALTER TABLE `testpkintint`
  ADD PRIMARY KEY (`pk1int`,`pk2int`),
  ADD UNIQUE KEY `uniqueLabelLanguage` (`label`,`language`),
  ADD KEY `IndexIdx99` (`idx12`,`idx22`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `customer`
--
ALTER TABLE `customer`
  MODIFY `customerId` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Customer identifier';
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
