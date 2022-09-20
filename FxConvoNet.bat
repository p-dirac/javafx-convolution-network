
setLocal

set jdkbase=C:\ZZ-java-tools\jdk-18.0.2.1
set jfxbase=C:\ZZ-java-tools\javafx-sdk-18.0.2

set jfx=%jfxbase%\bin
set jfxlibs=%jfxbase%\lib
set jdk=%jdkbase%\bin

set mainlib=.\target\classes
set data=..\datasets

set mylibs=%jfxlibs%\*;./lib/*;%data%;.\src\main\resources;%mainlib%
set mycp=.;./src;./build;%mylibs%


%jdk%\java --module-path %jfxlibs% --add-modules=javafx.controls -cp %mycp% datasci.frontend.ctrl.FxConvoMain

endLocal



