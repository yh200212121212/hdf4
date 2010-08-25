cmake_minimum_required (VERSION 2.8)
PROJECT (HDF4_MFHDF_NCDUMP)

INCLUDE_DIRECTORIES (${HDF4_HDFSOURCE_DIR})
INCLUDE_DIRECTORIES (${HDF4_MFHDFSOURCE_DIR})

ADD_DEFINITIONS (-DHDF)

IF (WIN32)
  ADD_DEFINITIONS (-DDOS_FS)
ENDIF (WIN32)

IF (HDF4_BUILD_XDR_LIB)
  IF (WIN32)
    ADD_DEFINITIONS (-DNO_SYS_XDR_INC)
  ENDIF (WIN32)
  INCLUDE_DIRECTORIES (${HDF4_MFHDF_XDR_DIR})
ENDIF (HDF4_BUILD_XDR_LIB)

SET (ncdump_SRCS
    ${HDF4_MFHDF_NCDUMP_SOURCE_DIR}/dumplib.c
    ${HDF4_MFHDF_NCDUMP_SOURCE_DIR}/ncdump.c
    ${HDF4_MFHDF_NCDUMP_SOURCE_DIR}/vardata.c
    ${HDF4_SOURCE_DIR}/mfhdf/util/getopt.c
)
  
ADD_EXECUTABLE (ncdump ${ncdump_SRCS})
IF (HDF4_BUILD_XDR_LIB)
  TARGET_LINK_LIBRARIES (ncdump ${HDF4_MF_LIB_TARGET} ${HDF4_SRC_LIB_TARGET} ${LINK_LIBS} ${HDF4_MF_XDR_LIB_TARGET} )
ELSE (HDF4_BUILD_XDR_LIB)
  TARGET_LINK_LIBRARIES (ncdump ${HDF4_MF_LIB_TARGET} ${HDF4_SRC_LIB_TARGET} ${LINK_LIBS})
ENDIF (HDF4_BUILD_XDR_LIB)
TARGET_NAMING (ncdump)
IF (WIN32)
  IF (MSVC)
    IF (NOT BUILD_SHARED_LIBS)
      SET_TARGET_PROPERTIES (ncdump PROPERTIES LINK_FLAGS "/NODEFAULTLIB:MSVCRT") 
    ENDIF (NOT BUILD_SHARED_LIBS)
  ENDIF (MSVC)
ENDIF (WIN32)

#-- Adding test for ncdump

#ADD_TEST (NAME testncdump COMMAND $<TARGET_FILE:testncdump>)
# 
##-- Copy all the dat files from the test directory into the source directory
#SET (HDF4_REFERENCE_TEST_FILES
#    sds_szipped.dat
#    smallslice.0000.nc
#    test1.nc
#    testout.sav
#)
# 
#FOREACH (h4_file ${HDF4_REFERENCE_TEST_FILES})
#  SET (dest "${PROJECT_BINARY_DIR}/${h4_file}")
#  #MESSAGE (STATUS " Copying ${HDF4_MFHDF_TEST_DIR}/${h4_file} to ${PROJECT_BINARY_DIR}/")
#  ADD_CUSTOM_COMMAND (
#      TARGET     ncdumptest 
#      POST_BUILD
#      COMMAND    ${CMAKE_COMMAND}
#      ARGS       -E copy_if_different ${HDF4_MFHDF_TEST_DIR}/${h4_file} ${dest}
#  )
#      
#ENDFOREACH (h4_file ${HDF4_REFERENCE_TEST_FILES})

#-----------------------------------------------------------------------------
# Rules for Installation of tools using make Install target
#-----------------------------------------------------------------------------
INSTALL (
    TARGETS
        ncdump
    RUNTIME DESTINATION
        bin/tools
    COMPONENT
        toolsapplications
)

