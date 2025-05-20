import Container from "@mui/material/Container";
import Typography from "@mui/material/Typography";
import Box from "@mui/material/Box";
import "./style.scss";

function Page404() {
  return (
    <Container maxWidth="sm">
      <Box sx={{ my: 4 }}>
        <Typography
          variant="h4"
          component="h1"
          gutterBottom
        >
          Page404
        </Typography>
      </Box>
    </Container>
  );
}

export { Page404 };
