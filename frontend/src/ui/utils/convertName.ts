export const convertName = (
  userName: string | undefined,
  isEmail: boolean | undefined
) => {
  if (!userName) return null;
  if (isEmail) {
    return userName?.substring(0, 1).toUpperCase();
  } else {
    const textConvert = userName.split(" ");
    if (textConvert.length > 1) {
      return (
        textConvert[0].substring(0, 1) + textConvert[1].substring(0, 1)
      ).toUpperCase();
    }
    return userName?.substring(0, 1).toUpperCase();
  }
};
