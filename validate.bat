rem SPDX-FileCopyrightText: 2024, 2025 SURF B.V.
rem SPDX-License-Identifier: EPL-2.0
rem SPDX-FileContributor: Joost Diepenmaat

@echo off

where /q bb
if ERRORLEVEL 0 (
 set RUNTIME="bb"
) else (
  set RUNTIME="clojure -M"
)
%RUNTIME% -m nl.surf.eduhub-validator.main %*
