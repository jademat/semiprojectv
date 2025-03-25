package com.example.zzyzzy.semiprojectv1.apicontroller;

import com.example.zzyzzy.semiprojectv1.domain.BoardDTO;
import com.example.zzyzzy.semiprojectv1.domain.BoardListDTO;
import com.example.zzyzzy.semiprojectv1.domain.BoardReplyDTO;
import com.example.zzyzzy.semiprojectv1.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
public class ApiBoardController {

    private final BoardService boardService;

    @GetMapping("/list")
    public ResponseEntity<BoardListDTO> list(@RequestParam(defaultValue = "1") int cpg){

        BoardListDTO boardListDTO= boardService.readBoard(cpg);

        return new ResponseEntity<>(boardListDTO, HttpStatus.OK);
    }
    @GetMapping("/find")
    public ResponseEntity<BoardListDTO> find( String findtype, String findkey,
                @RequestParam(defaultValue = "1") int cpg) {

        BoardListDTO boardListDTO= boardService.findBoard(cpg,findtype,findkey);

        return new ResponseEntity<>(boardListDTO, HttpStatus.OK);
    }

    @GetMapping("/view")
    public ResponseEntity<BoardReplyDTO> view(int bno) {

        BoardReplyDTO boardReplyDTO = boardService.readOneBoardReply(bno);

        return new ResponseEntity<>(boardReplyDTO, HttpStatus.OK);
    }

}
