;This file will be executed next to the application bundle image
;I.e. current directory will contain folder UserLogin2-1.0 with application files
[Setup]
AppId={{com.idi.userlogin}}
AppName=UserLogin2-1.0
AppVersion=1.0
AppVerName=UserLogin2-1.0
AppPublisher=idi
AppComments=UserLogin2-1.0
AppCopyright=Copyright (C) 2021
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
AppendDefaultDirName=No
UsePreviousAppDir=No
DefaultDirName={sd}\IDI\UserLogin2
DisableStartupPrompt=Yes
DisableDirPage=No
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=idi
;Optional License
LicenseFile=
;(Windows 2000/XP/Server 2003 are no longer supported.)
MinVersion=6.0
OutputBaseFilename=UserLogin2-1.0
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=UserLogin2-1.0\UserLogin2-1.0.ico
UninstallDisplayIcon={app}\UserLogin2-1.0.ico
UninstallDisplayName=UserLogin2-1.0
WizardImageStretch=No
WizardSmallImageFile=UserLogin2-1.0-setup-icon.bmp
ArchitecturesInstallIn64BitMode=x64


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; \
    GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "UserLogin2-1.0\UserLogin2-1.0.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "UserLogin2-1.0\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\UserLogin2-1.0"; Filename: "{app}\UserLogin2-1.0.exe"; IconFilename: "{app}\UserLogin2-1.0.ico"; Check: returnTrue()
Name: "{commondesktop}\UserLogin2-1.0"; Filename: "{app}\UserLogin2-1.0.exe";  IconFilename: "{app}\UserLogin2-1.0.ico"; Check: returnTrue()
Name: "{userdesktop}\UserLogin2-1.0"; Filename: "{app}\UserLogin2-1.0.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\UserLogin2-1.0.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\UserLogin2-1.0.exe"; Description: "{cm:LaunchProgram,UserLogin2-1.0}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\UserLogin2-1.0.exe"; Parameters: "-install -svcName ""UserLogin2-1.0"" -svcDesc ""UserLogin2-1.0"" -mainExe ""UserLogin2-1.0.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\UserLogin2-1.0.exe "; Parameters: "-uninstall -svcName UserLogin2-1.0 -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
