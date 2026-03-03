REM SPDX-FileCopyrightText: 2024, 2025, 2026 SURF B.V.
REM
REM SPDX-License-Identifier: EPL-2.0 WITH Classpath-exception-2.0

@echo off

where /q bb
if ERRORLEVEL 0 (
 set RUNTIME="bb"
) else (
  set RUNTIME="clojure -M"
)
%RUNTIME% -m nl.surf.eduhub-validator.main %*
