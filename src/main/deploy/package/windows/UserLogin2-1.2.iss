;This file will be executed next to the application bundle image
;I.e. current directory will contain folder UserLogin2-1.2 with application files
[Setup]
AppId={{com.idi.userlogin}}
AppName=UserLogin2-1.2
AppVersion=1.2
AppVerName=UserLogin2-1.2
AppPublisher=idi
AppComments=UserLogin2-1.2
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
OutputBaseFilename=UserLogin2-1.2
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=UserLogin2-1.2\UserLogin2-1.2.ico
UninstallDisplayIcon={app}\UserLogin2-1.2.ico
UninstallDisplayName=UserLogin2-1.2
WizardImageStretch=No
WizardSmallImageFile=UserLogin2-1.2-setup-icon.bmp
ArchitecturesInstallIn64BitMode=x64


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; \
    GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "UserLogin2-1.2\UserLogin2-1.2.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "UserLogin2-1.2\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\UserLogin2-1.2"; Filename: "{app}\UserLogin2-1.2.exe"; IconFilename: "{app}\UserLogin2-1.2.ico"; Check: returnTrue()
Name: "{commondesktop}\UserLogin2-1.2"; Filename: "{app}\UserLogin2-1.2.exe";  IconFilename: "{app}\UserLogin2-1.2.ico"; Check: returnTrue()
Name: "{userdesktop}\UserLogin2-1.2"; Filename: "{app}\UserLogin2-1.2.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\UserLogin2-1.2.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\UserLogin2-1.2.exe"; Description: "{cm:LaunchProgram,UserLogin2-1.2}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\UserLogin2-1.2.exe"; Parameters: "-install -svcName ""UserLogin2-1.2"" -svcDesc ""UserLogin2-1.2"" -mainExe ""UserLogin2-1.2.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\UserLogin2-1.2.exe "; Parameters: "-uninstall -svcName UserLogin2-1.2 -stopOnUninstall"; Check: returnFalse()

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
