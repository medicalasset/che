#!/bin/sh
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

init_logging() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  NC='\033[0m'
}

init_global_variables() {

  CHE_LAUNCHER_CONTAINER_NAME="che-launcher"
  CHE_LAUNCHER_IMAGE_NAME="codenvy/che-launcher"
  CHE_SERVER_IMAGE_NAME="codenvy/che-server"

  CHE_MOUNT_FOLDER=${CHE_MOUNT_FOLDER:+$(get_clean_path ${CHE_MOUNT_FOLDER})}

  # User configurable variables
  DEFAULT_CHE_VERSION="latest"
  DEFAULT_CHE_CLI_ACTION="help"

  CHE_VERSION=${CHE_VERSION:-${DEFAULT_CHE_VERSION}}
  CHE_CLI_ACTION=${CHE_CLI_ACTION:-${DEFAULT_CHE_CLI_ACTION}}

  USAGE="
Usage: che [COMMAND]
           start                              Starts Che server
           stop                               Stops Che server
           restart                            Restart Che server
           update                             Pull latest version of ${CHE_LAUNCHER_IMAGE_NAME}
           info                               Print some debugging information
           mount <local-path> <ws-ssh-port>   Synchronize workspace to a local directory
           init                               Initialize directory with Che configuration
           up                                 Create workspace from source in current directory
"
}

usage () {
  printf "%s" "${USAGE}"
}

info() {
  printf  "${GREEN}INFO:${NC} %s\n" "${1}"
}

debug() {
  printf  "${BLUE}DEBUG:${NC} %s\n" "${1}"
}

error() {
  printf  "${RED}ERROR:${NC} %s\n" "${1}"
}

error_exit() {
  echo  "---------------------------------------"
  error "!!!"
  error "!!! ${1}"
  error "!!!"
  echo  "---------------------------------------"
  exit 1
}

get_full_path() {
  echo $(realpath $1)
}

convert_windows_to_posix() {
  echo "/"$(echo "$1" | sed 's/\\/\//g' | sed 's/://')
}

get_clean_path() {
  INPUT_PATH=$1
  # \some\path => /some/path
  OUTPUT_PATH=$(echo ${INPUT_PATH} | tr '\\' '/')
  # /somepath/ => /somepath
  OUTPUT_PATH=${OUTPUT_PATH%/}
  # /some//path => /some/path
  OUTPUT_PATH=$(echo ${OUTPUT_PATH} | tr -s '/')
  # "/some/path" => /some/path
  OUTPUT_PATH=${OUTPUT_PATH//\"}
  echo ${OUTPUT_PATH}
}

get_mount_path() {
  FULL_PATH=$(get_full_path $1)
  POSIX_PATH=$(convert_windows_to_posix $FULL_PATH)
  echo $(get_clean_path $POSIX_PATH)
}


docker-exec() {
  if is_boot2docker || is_docker_for_windows; then
    MSYS_NO_PATHCONV=1 docker.exe "$@"
  else
    echo $(get_docker_install_type)
    "$(which docker)" "$@"
  fi
}

check_docker() {
  if ! docker ps > /dev/null 2>&1; then
    output=$(docker)
    error_exit "Error - Docker not installed properly: ${output}"
  fi
}

parse_command_line () {
  if [ $# -eq 0 ]; then 
    CHE_CLI_ACTION="help"
  else
    case $1 in
      start|stop|restart|update|info|init|up|mount|help|-h|--help)
        CHE_CLI_ACTION=$1
      ;;
      *)
        # unknown option
        error_exit "You passed an unknown command line option."
      ;;
    esac
  fi
}

is_boot2docker() {
  if uname -r | grep -q 'boot2docker'; then
    return 0
  else
    return 1
  fi
}

get_docker_host_ip() {
  NETWORK_IF="eth0"
  if is_boot2docker; then
    NETWORK_IF="eth1"
  fi

  docker run --rm --net host \
            alpine sh -c \
            "ip a show ${NETWORK_IF}" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{ print $2}'
}

has_docker_for_windows_ip() {
  DOCKER_HOST_IP=$(get_docker_host_ip)
  if [ "${DOCKER_HOST_IP}" = "10.0.75.2" ]; then
    return 0
  else
    return 1
  fi
}

is_moby_vm() {
  if [ $(docker info | grep "Name:" | cut -d" " -f2) = "moby" ]; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  if is_moby_vm && ! has_docker_for_windows_ip; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  if is_moby_vm && has_docker_for_windows_ip; then
    return 0
  else
    return 1
  fi
}

get_docker_install_type() {
  if is_boot2docker; then
    echo "boot2docker"
  elif is_docker_for_windows; then
    echo "docker4windows"
  elif is_docker_for_mac; then
    echo "docker4mac"
  else
    echo "native"
  fi
}

get_list_of_variables() {
  RETURN=""
  CHE_VARIABLES=$(env | grep "CHE_")
  for SINGLE_VARIABLE in $CHE_VARIABLES; do
    VALUE='-e '${SINGLE_VARIABLE}' '
    RETURN="${RETURN}""${VALUE}"
  done
  echo $RETURN
}

execute_che_launcher() {

  CURRENT_IMAGE=$(docker images -q "${CHE_LAUNCHER_IMAGE_NAME}":"${CHE_VERSION}")

  if [ "${CURRENT_IMAGE}" != "" ]; then
    info "ECLIPSE CHE: FOUND IMAGE ${CHE_LAUNCHER_IMAGE_NAME}:${CHE_VERSION}"
  else
    update_che_image ${CHE_LAUNCHER_IMAGE_NAME}
  fi
  
  info "ECLIPSE CHE: LAUNCHING LAUNCHER"
  docker-exec run -t --name "${CHE_LAUNCHER_CONTAINER_NAME}" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -e $(get_list_of_variables) \
    "${CHE_LAUNCHER_IMAGE_NAME}":"${CHE_VERSION}" "${CHE_CLI_ACTION}" \
    # > /dev/null 2>&1
}

execute_che_file() {

  update_che_file

  info "ECLIPSE CHE FILE: LAUNCHING CONTAINER"
  CURRENT_DIRECTORY="$PWD"
  MODIFIED_DIRECTORY=${CURRENT_DIRECTORY//////}
  docker-exec run -it --rm --name "${CHE_FILE_CONTAINER_NAME}" \
         -v /var/run/docker.sock:/var/run/docker.sock \
         -v "$PWD":"$PWD" \
         "${CHE_FILE_IMAGE_NAME}":"${CHE_VERSION}" \
         /bin/che-file "$PWD" "${CHE_CLI_ACTION}"
    # > /dev/null 2>&1
}

execute_command_with_progress() {
  local progress=$1
  local command=$2
  shift 2

  local pid=""
  printf "\n"

  case "$progress" in
    extended)
      $command "$@"
      ;;
    basic|*)
      $command "$@" &>/dev/null &
      pid=$!
      while kill -0 "$pid" >/dev/null 2>&1; do
        printf "#"
        sleep 10
      done
      wait $pid # return pid's exit code
      printf "\n"
    ;;
  esac
  printf "\n"
}

update_che_image() {
  if [ -z "${CHE_VERSION}" ]; then
    CHE_VERSION=${DEFAULT_CHE_VERSION}
  fi

  info "ECLIPSE CHE: PULLING IMAGE $1:${CHE_VERSION}"
  execute_command_with_progress extended docker pull $1:${CHE_VERSION}
  info "ECLIPSE CHE: IMAGE $1:${CHE_VERSION} INSTALLED"
}

mount_local_directory() {
  if [ ! $# -eq 3 ]; then 
    error "che mount: Wrong number of arguments provided."
    return
  fi

  MOUNT_PATH=$(get_mount_path $2)

  if [ ! -e "${MOUNT_PATH}" ]; then
    error "che mount: Path provided does not exist."
    return
  fi

  if [ ! -d "${MOUNT_PATH}" ]; then
    error "che mount: Path provided is not a valid directory."
    return
  fi

  docker-exec run --rm -it --cap-add SYS_ADMIN \
                  --device /dev/fuse \
                  --name che-mount \
                  -v "${MOUNT_PATH}":/mnthost \
                  codenvy/che-mount $(get_docker_host_ip) $3
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

init_logging
check_docker
init_global_variables
parse_command_line "$@"

case ${CHE_CLI_ACTION} in
  start|stop|restart|info)
    execute_che_launcher
  ;;
  init|up)
    execute_che_file
  ;;
  update)
    update_che_image ${CHE_LAUNCHER_IMAGE_NAME}
    update_che_image ${CHE_SERVER_IMAGE_NAME}
  ;;
  mount)
    mount_local_directory "$@"
  ;;
  help)
#    get_list_of_variables
    usage
  ;;
esac
