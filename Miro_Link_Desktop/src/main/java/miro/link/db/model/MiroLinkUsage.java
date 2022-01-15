package miro.link.db.model;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class MiroLinkUsage {

    private int userID;
    private int usages;
    private long totalUsageTime;

}
