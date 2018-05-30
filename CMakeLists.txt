cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_STANDARD 14)

add_library( regrep
             SHARED
             src/main/jni/regrep.cpp )

find_library( android-lib android )

target_compile_options( regrep PRIVATE
                        "$<$<CONFIG:RELEASE>:-O3>"
                        "$<$<CONFIG:DEBUG>:-O3>" )

target_link_libraries( regrep
                       ${android-lib} )